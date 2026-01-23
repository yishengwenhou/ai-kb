package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.DocumentMapper;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Resource
    private OssUtil ossUtil;

    @Resource
    private  DocumentAsyncServiceImpl documentAsyncService;

    @Override
    public Document uploadAndSave(MultipartFile file, Long kbId, Long parentId) {
        try {
            // 1. 计算哈希 (MD5 或 SHA-256)
            String fileHash = DigestUtils.md5DigestAsHex(file.getInputStream());

            // 2. 查库：是否已存在相同哈希的文件？
            Document existDoc = this.getOne(new LambdaQueryWrapper<Document>()
                    .eq(Document::getFileHash, fileHash)
                    .last("LIMIT 1")); // 只要查到一个就行

            if (existDoc != null) {
                Document newDoc = Document.builder()
                        .kbId(kbId)
                        .fileName(file.getOriginalFilename())
                        .fileType(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1))
                        .filePath(existDoc.getFilePath()) // 复用 OSS 路径
                        .fileSize(file.getSize())
                        .fileHash(fileHash)
                        .parentId(parentId)
                        .build();

                // 【优化】检查旧文件是否已经解析好了
                if (existDoc.getStatus() == 0 && existDoc.getContent() != null) {
                    // 秒传的核心：直接复用解析结果！
                    newDoc.setContent(existDoc.getContent());
                    newDoc.setCharCount(existDoc.getCharCount());
                    newDoc.setStatus(0); // 直接就绪
                    this.save(newDoc);
                    // 直接返回，【不】调用 parseDocument
                    return newDoc;
                } else {
                    // 旧文件可能解析失败了，或者正在解析中，那新文件也标记为处理中，重新尝试解析
                    newDoc.setStatus(1);
                    this.save(newDoc);
                    documentAsyncService.parseDocument(newDoc.getId());
                    return newDoc;
                }
            }

            // 3. 上传 OSS
            String url = ossUtil.upload(file.getInputStream(), file.getOriginalFilename());

            // 4. 构建并保存 Document 对象
            Document doc = Document.builder()
                    .kbId(kbId)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1))
                    .filePath(url)
                    .fileSize(file.getSize())
                    .fileHash(fileHash)
                    .parentId(parentId)
                    .status(1) // 1-处理中
                    .build();

            this.save(doc);

            // 5. 【关键修复】手动触发异步解析
            documentAsyncService.parseDocument(doc.getId());

            return doc;

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败", e);
        }
    }

    @Override
    public boolean deleteDocument(Long id) {
        // 1. 先查询文档信息
        Document doc = this.getById(id);
        if (doc == null) {
            return false;
        }

        // 2. 检查文件夹逻辑 (保持不变)
        if (doc.getIsFolder() == 1) {
            Long count = baseMapper.selectCount(
                    new LambdaQueryWrapper<Document>().eq(Document::getParentId, id)
            );
            if (count > 0) {
                throw new RuntimeException("该目录下仍有 " + count + " 个子文档，请先清空后再删除！");
            }
        }

        if (doc.getIsFolder() == 0 && doc.getFileHash() != null) {
            // 查询数据库中，拥有相同 file_hash 的文档数量
            Long refCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<Document>()
                            .eq(Document::getFileHash, doc.getFileHash())
            );

            if (refCount == 1) {
                try {
                    ossUtil.delete(doc.getFilePath());
                    log.info("OSS文件已物理删除: {}", doc.getFilePath());
                } catch (Exception e) {
                    log.error("OSS文件删除失败: {}", doc.getFilePath());
                }
            } else {
                log.info("该文件仍有 {} 处引用，跳过物理删除，仅删除数据库记录", refCount);
            }
        }

        // 4. 删除数据库记录
        return this.removeById(id);
    }

    @Override
    public List<Document> getListByKbId(Long kbId) {
        return this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getKbId, kbId)
                .orderByDesc(Document::getCreateTime));
    }


    @Override
    public PreviewResult preview(Long id) {
        Document doc = baseMapper.selectById(id);
        if (doc == null) throw new RuntimeException("文件不存在");

        try {
            // 1. 下载文件流
            java.io.InputStream inputStream = ossUtil.download(doc.getFilePath());
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            // 2. 猜测文件类型 (MIME Type)
            String ext = doc.getFileType().toLowerCase();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            // 根据后缀设置正确的 MIME，这样浏览器才知道怎么渲染
            if ("pdf".equals(ext)) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if ("png".equals(ext)) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("txt".equals(ext)) {
                mediaType = MediaType.TEXT_PLAIN;
            }

            // 3. 返回响应 (inline 表示在线显示)
            return new PreviewResult(doc.getFileName(), bytes, mediaType);

        } catch (Exception e) {
            throw new RuntimeException("文件处理失败", e);
        }
    }
}
