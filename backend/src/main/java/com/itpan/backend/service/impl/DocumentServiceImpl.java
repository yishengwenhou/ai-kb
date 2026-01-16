package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.DocumentMapper;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Resource
    private OssUtil ossUtil;

    @Override
    public Document uploadAndSave(MultipartFile file, Long kbId) {
        // 1. 上传到 OSS
        String url;
        try {
            url = ossUtil.upload(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("文件流读取失败", e);
        }

        // 2. 封装 Document 对象
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        Document doc = Document.builder()
                .kbId(kbId)
                .fileName(fileName)
                .fileType(suffix)
                .filePath(url)
                .fileSize(file.getSize())
                .status(1)
                .build();

        // 3. 落库
        this.save(doc);

        // 4. TODO: 这里将来要发送一个事件或调用异步方法，开始解析文档
        // asyncParseService.parse(doc.getId());

        return doc;
    }

    @Override
    public List<Document> getListByKbId(Long kbId) {
        return this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getKbId, kbId)
                .orderByDesc(Document::getCreateTime));
    }

}
