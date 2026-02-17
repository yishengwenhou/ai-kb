package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.common.constants.OssConstant;
import com.itpan.backend.mapper.DocumentContentMapper;
import com.itpan.backend.mapper.DocumentMapper;
import com.itpan.backend.mapper.UserMapper;
import com.itpan.backend.model.dto.document.DocContentUpdateDTO;
import com.itpan.backend.model.dto.document.DocCreateDTO;
import com.itpan.backend.model.dto.document.MoveNodeDTO;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.DocumentContent;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.DocumentVO;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Resource
    private OssUtil ossUtil;

    @Resource
    private DocumentAsyncServiceImpl documentAsyncService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DocumentContentMapper documentContentMapper;

    @Transactional(rollbackFor = Exception.class)
    public Document createNode(DocCreateDTO docCreateDTO) {
        Document node = Document.builder()
                .kbId(docCreateDTO.getKbId())
                .parentId(docCreateDTO.getParentId())
                .title(docCreateDTO.getTitle())
                .type(docCreateDTO.getType())
                .build();
        return createNode(node); // 调用上面的核心逻辑
    }

    @Transactional(rollbackFor = Exception.class)
    public Document createNode(Document node) {
        // 1. 校验父节点
        if (node.getParentId() != null && node.getParentId() != 0L) {
            Document parent = this.getById(node.getParentId());
            if (parent == null) {
                throw new RuntimeException("父节点不存在");
            }

            // 【新增】校验 1：防止跨知识库创建
            if (!parent.getKbId().equals(node.getKbId())) {
                throw new RuntimeException("父节点不属于当前知识库");
            }

            // 【新增】校验 2：禁止在纯文件节点下创建子节点
            if ("file".equals(parent.getType())) {
                throw new RuntimeException("无法在文件节点下创建内容");
            }

            // 拼接 TreePath
            node.setTreePath(parent.getTreePath() + parent.getId() + "/");
        } else {
            node.setTreePath("0/");
        }

        // 2. 计算 Sort (并发不安全，但毕设场景可接受)
        Double maxSort = baseMapper.selectMaxSort(node.getKbId(), node.getParentId());
        node.setSort(maxSort == null ? 65536.0 : maxSort + 65536.0);

        // 3. 设置默认图标
        if (node.getIcon() == null) {
            node.setIcon(getDefaultIcon(node.getType()));
        }

        // 4. 初始化其他字段
        node.setStatus(0); // 默认正常状态

        baseMapper.insert(node);
        documentContentMapper.insert(
                DocumentContent.builder()
                .id(node.getId())
                .content("")
                .build()
        );

        return node;
    }

    public List<DocumentVO> getChildren(Long kbId, Long parentId) {
        // 假设你在 Mapper 里写了 selectNodeList
        return baseMapper.selectNodeList(kbId, parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveNode(MoveNodeDTO moveNodeDTO) {
        Long id = moveNodeDTO.getId();
        Long targetParentId = moveNodeDTO.getTargetParentId();
        Double newSort = moveNodeDTO.getNewSort();

        // 1. 查当前节点
        Document node = this.getById(id);
        if (node == null) throw new RuntimeException("节点不存在");

        // 2. 基础检查：不能移动到自己下面 (ID相等)
        if (id.equals(targetParentId)) {
            throw new RuntimeException("不能移动到自己下面");
        }

        // 3. 处理层级变化 (Parent 变了)
        if (!node.getParentId().equals(targetParentId)) {
            String newTreePath = "0/";

            // 如果不是移动到根目录，需要检查目标父节点
            if (targetParentId != 0) {
                Document targetParent = this.getById(targetParentId);
                if (targetParent == null) throw new RuntimeException("目标父节点不存在");

                // 【关键修复】严谨的死循环检查 (Circular Dependency Check)
                // 检查规则：目标父节点的路径中，不能包含当前节点的ID。
                // 例如：要把 A 移动到 B 下面，必须确保 B 不是 A 的子孙。
                // 构造目标父节点的完整路径判据：path + id + /
                String targetFullPath = targetParent.getTreePath() + targetParent.getId() + "/";

                // 必须带上分隔符 "/" 匹配，防止 ID=1 和 ID=11 混淆
                String nodePathIdentifier = "/" + node.getId() + "/";

                if (targetFullPath.contains(nodePathIdentifier)) {
                    throw new RuntimeException("非法操作：不能将节点移动到自己的子孙节点下");
                }

                // 计算新路径：父路径 + 父ID + /
                newTreePath = targetFullPath;
            }

            // 【关键修复】必须在修改 node 对象之前，先计算旧路径前缀！
            // 你的原代码在 setTreePath 之后才算 oldPrefix，那是错的，会导致子节点路径更新失效。
            String oldTreePathPrefix = node.getTreePath() + node.getId() + "/";
            String newTreePathPrefix = newTreePath + node.getId() + "/";

            // 🚀 一条 SQL 批量更新所有子孙节点的 tree_path
            baseMapper.updateTreePathByParent(oldTreePathPrefix, newTreePathPrefix, node.getKbId());

            // 更新当前节点的指针
            node.setParentId(targetParentId);
            node.setTreePath(newTreePath);
        }

        // 4. 处理排序变化
        // 如果前端没传 sort (比如只是纯粹的“移动到文件夹”，不指定位置)，默认放到最后
        if (newSort == null) {
            Double maxSort = baseMapper.selectMaxSort(node.getKbId(), targetParentId);
            // 初始值为 65536，间隔也用 65536，方便后续插入
            newSort = (maxSort == null ? 65536.0 : maxSort + 65536.0);
        }
        node.setSort(newSort);

        // 5. 保存当前节点变更
        this.updateById(node);
    }

    /**
     * 上传文件 (适配新结构)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document uploadFile(MultipartFile file, Long kbId, Long parentId) {
        try {
            // 1. 基础校验
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            String originalFilename = file.getOriginalFilename();
            // 获取后缀，例如 "pdf"
            String ext = FilenameUtils.getExtension(originalFilename);
            // 如果没有 commons-io，可以用:
            // String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            // 2. 计算哈希 (用于秒传检测)
            String fileHash = DigestUtils.md5DigestAsHex(file.getInputStream());

            // 3. 【去重逻辑】检查是否已存在相同 Hash 的文件
            // 如果存在，直接复用其 OSS 路径 (fileUrl)，不再上传
            Document existDoc = this.getOne(new LambdaQueryWrapper<Document>()
                    .eq(Document::getFileHash, fileHash)
                    .last("LIMIT 1"));

            String fileUrl;
            if (existDoc != null && existDoc.getFileUrl() != null) {
                fileUrl = existDoc.getFileUrl(); // 秒传：复用旧 URL
            } else {
                // 4. 真实上传到 OSS
                // 参数：Bucket名, 文件流, 文件名
                fileUrl = ossUtil.upload(OssConstant.BUCKET_DOCS, file.getInputStream(), originalFilename);
            }

            // 5. 构建节点对象 (适配新表结构)
            Document node = Document.builder()
                    .kbId(kbId)
                    .parentId(parentId)
                    .title(originalFilename)           // 新字段：标题
                    .type("file")                      // 新字段：类型固定为 file
                    .fileUrl(fileUrl)                  // 新字段：文件路径
                    .fileExt(ext)                      // 新字段：后缀
                    .fileSize(file.getSize())          // 新字段：大小
                    .fileHash(fileHash)                // 新字段：哈希
                    .status(0)                         // 0-就绪 (若需异步解析内容，可设为 1)
                    .build();

            // 6. 调用统一创建方法 (自动计算 treePath 和 sort)
            return createNode(node);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    // 辅助方法
    private String getDefaultIcon(String type) {
        return switch (type) {
            case "folder" -> "📁";
            case "doc" -> "📄";
            case "sheet" -> "📊";
            case "file" -> "📎";
            default -> "❓";
        };
    }

    // DocumentServiceImpl.java

    @Override
    public DocumentVO getNodeDetail(Long id) {
        Document doc = this.getById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }
        DocumentContent docContent = documentContentMapper.selectById(id);

        DocumentVO vo = DocumentVO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .type(doc.getType())
                .sort(doc.getSort())
                .parentId(doc.getParentId())
                .treePath(doc.getTreePath())
                .icon(doc.getIcon())
                .description(doc.getDescription())
                .content(docContent.getContent())
                .build();

        // 3. 特殊处理
        if ("file".equals(doc.getType())) {
            // 如果是文件：content 置空，确保 fileUrl 有值
            vo.setContent(null);
            // 实际上数据库里 fileUrl 应该已经有值了
            // 如果是私有读 bucket，这里可能需要生成带签名的临时 URL
            // vo.setFileUrl(ossUtil.generatePresignedUrl(doc.getFileUrl()));
        } else {
            // 如果是 doc/sheet：确保 content 返回给前端
            // 你的数据库 content 字段存的就是编辑器产生的 HTML/JSON
            vo.setFileUrl(null); // 在线文档不需要下载链接
        }

        // 4. 补充其他信息（如创建人名字）
        User user = userMapper.selectById(doc.getCreateBy());
        if (user != null) {
            vo.setCreateName(user.getRealName());
        }

        return vo;
    }



    @Override
    public boolean updateContent(DocContentUpdateDTO docContentUpdateDTO) {

        documentContentMapper.update(
                new LambdaUpdateWrapper<DocumentContent>()
                        .eq(DocumentContent::getId, docContentUpdateDTO.getId())
                        .set(DocumentContent::getContent, docContentUpdateDTO.getContent())
                        .set(DocumentContent::getContentHtml, docContentUpdateDTO.getContentHtml())
        );
        return true;
    }

//    @Override
//    public void updateMeta(DocMetaUpdateDTO dto) {
//        this.update(new LambdaUpdateWrapper<Document>()
//                .eq(Document::getId, dto.getId())
//                .set(dto.getTitle() != null, Document::getTitle, dto.getTitle())
//                .set(dto.getIcon() != null, Document::getIcon, dto.getIcon()));
//    }

    @Override
    public List<DocumentVO> getBreadcrumb(Long id) {
        Document doc = this.getById(id);
        if (doc == null) return new ArrayList<>();

        String treePath = doc.getTreePath(); // 例如 "0/1/5/"
        if (StringUtils.isEmpty(treePath) || "0/".equals(treePath)) {
            return new ArrayList<>();
        }

        // 解析 IDs: "0/1/5/" -> [1, 5]
        String[] split = treePath.split("/");
        List<Long> parentIds = new ArrayList<>();
        for (String s : split) {
            if (!"0".equals(s) && StringUtils.hasText(s)) {
                parentIds.add(Long.parseLong(s));
            }
        }

        if (parentIds.isEmpty()) return new ArrayList<>();

        // 批量查询父节点，并按 ID 排序（注意：数据库查出来是无序的，需要内存重排）
        List<Document> parents = baseMapper.selectBatchIds(parentIds);

        // 按照 treePath 的顺序重排
        Map<Long, Document> map = parents.stream().collect(Collectors.toMap(Document::getId, p -> p));
        List<DocumentVO> result = new ArrayList<>();
        for (Long pid : parentIds) {
            if (map.containsKey(pid)) {
                // 转 VO
                DocumentVO vo = new DocumentVO();
                BeanUtils.copyProperties(map.get(pid), vo);
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public void removeNode(Long id) {
        // 1. 检查是否有子节点
        Long childrenCount = baseMapper.selectCount(new LambdaQueryWrapper<Document>()
                .eq(Document::getParentId, id)
                .eq(Document::getDeleted, 0)); // 只查没删除的

        if (childrenCount > 0) {
            throw new RuntimeException("请先删除该文件夹下的所有内容");
        }

        // 2. 逻辑删除
        this.update(new LambdaUpdateWrapper<Document>()
                .eq(Document::getId, id)
                .set(Document::getDeleted, 1) // 1 表示进入回收站
                .set(Document::getStatus, 1));
    }

    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        Document doc = this.getById(id);
        if (doc == null || !"file".equals(doc.getType())) {
            throw new RuntimeException("文件不存在或类型错误");
        }

        try {
            // 设置响应头
            response.setContentType("application/octet-stream");
            String fileName = URLEncoder.encode(doc.getTitle(), "UTF-8"); // 处理中文文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 从 OSS 获取流并写入 Response
            InputStream is = ossUtil.download(doc.getFileUrl());
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("下载失败", e);
        }
    }

}
