package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.mapper.*;
import com.itpan.backend.model.dto.RecycleBinItem;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.RecycleBinService;
import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回收站服务实现
 */
@Slf4j
@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OssUtil ossUtil;

    @Override
    public IPage<RecycleBinItem> getRecycleBinList(String type, String keyword, int pageNum, int pageSize) {
//        Page<RecycleBinItem> page = new Page<>(pageNum, pageSize);
//        List<RecycleBinItem> items = new ArrayList<>();
//
//        // 查询已删除的文档
//        if ("all".equals(type) || "document".equals(type)) {
//            Page<Document> docPage = new Page<>(pageNum, pageSize);
//            IPage<Document> deletedDocs = documentMapper.selectDeletedPage(docPage, keyword);
//
//            for (Document doc : deletedDocs.getRecords()) {
//                RecycleBinItem item = RecycleBinItem.builder()
//                        .id(doc.getId())
//                        .type("document")
//                        .name(doc.getFileName())
//                        .kbId(doc.getKbId())
//                        .fileType(doc.getFileType())
//                        .fileSize(doc.getFileSize())
//                        .deleteTime(doc.getUpdateTime())
//                        .deletedBy(doc.getUpdateBy())
//                        .build();
//
//                // 查询知识库名称
//                if (doc.getKbId() != null) {
//                    KnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKbId());
//                    if (kb != null) {
//                        item.setKbName(kb.getName());
//                    }
//                }
//
//                // 查询删除人名称
//                if (doc.getUpdateBy() != null) {
//                    User user = userMapper.selectById(doc.getUpdateBy());
//                    if (user != null) {
//                        item.setDeletedByName(user.getUsername());
//                    }
//                }
//
//                items.add(item);
//            }
//        }
//
//        // 查询已删除的知识库
//        if ("all".equals(type) || "knowledgeBase".equals(type)) {
//            Page<KnowledgeBase> kbPage = new Page<>(pageNum, pageSize);
//            IPage<KnowledgeBase> deletedKbs = knowledgeBaseMapper.selectDeletedPage(kbPage, keyword);
//
//            for (KnowledgeBase kb : deletedKbs.getRecords()) {
//                RecycleBinItem item = RecycleBinItem.builder()
//                        .id(kb.getId())
//                        .type("knowledgeBase")
//                        .name(kb.getName())
//                        .ownerType(kb.getOwnerType())
//                        .ownerId(kb.getOwnerId())
//                        .description(kb.getDescription())
//                        .deleteTime(kb.getUpdateTime())
//                        .deletedBy(kb.getUpdateBy())
//                        .build();
//
//                // 查询删除人名称
//                if (kb.getUpdateBy() != null) {
//                    User user = userMapper.selectById(kb.getUpdateBy());
//                    if (user != null) {
//                        item.setDeletedByName(user.getUsername());
//                    }
//                }
//
//                items.add(item);
//            }
//        }
//
//        // 按删除时间倒序排序
//        items.sort((a, b) -> b.getDeleteTime().compareTo(a.getDeleteTime()));
//
//        // 手动分页
//        int fromIndex = (pageNum - 1) * pageSize;
//        int toIndex = Math.min(fromIndex + pageSize, items.size());
//        List<RecycleBinItem> pagedItems = items.subList(
//                Math.min(fromIndex, items.size()),
//                toIndex
//        );
//
//        page.setRecords(pagedItems);
//        page.setTotal(items.size());
//
//        return page;
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreDocument(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }

        // 检查知识库是否存在（包括已删除的）
        KnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKbId());
        if (kb == null) {
            throw new RuntimeException("所属知识库不存在，无法恢复文档");
        }
        if (kb.getDeleted() == 1) {
            throw new RuntimeException("所属知识库已被删除，请先恢复知识库");
        }

        // 恢复：deleted = 0
        doc.setDeleted(0);
        return documentMapper.updateById(doc) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreKnowledgeBase(Long id) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) {
            throw new RuntimeException("知识库不存在");
        }


        kb.setDeleted(0);
        return knowledgeBaseMapper.updateById(kb) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean permanentDeleteDocument(Long id) {
//        Document doc = documentMapper.selectById(id);
//        if (doc == null) {
//            throw new RuntimeException("文档不存在");
//        }
//
//        // 处理 OSS 文件（引用计数为1时删除）
//        if (doc.getFileHash() != null) {
//            Long refCount = documentMapper.selectCount(
//                    new LambdaQueryWrapper<Document>()
//                            .eq(Document::getFileHash, doc.getFileHash())
//            );
//            if (refCount == 1) {
//                try {
//                    ossUtil.delete(doc.getFilePath());
//                    log.info("OSS文件删除成功: {}", doc.getFilePath());
//                } catch (Exception e) {
//                    log.warn("OSS文件删除失败: {}, 错误: {}", doc.getFilePath(), e.getMessage());
//                }
//            }
//        }
//
//        // 物理删除
//        return documentMapper.permanentDelete(id) > 0;
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean permanentDeleteKnowledgeBase(Long id) {
        // 先删除知识库下的所有文档
        List<Document> docs = documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getKbId, id)
                        .eq(Document::getDeleted, 1)
        );

        for (Document doc : docs) {
            permanentDeleteDocument(doc.getId());
        }

        // 物理删除知识库
        return knowledgeBaseMapper.permanentDelete(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRestore(List<Long> ids, String type) {
        int count = 0;
        for (Long id : ids) {
            try {
                if ("document".equals(type)) {
                    if (restoreDocument(id)) {
                        count++;
                    }
                } else if ("knowledgeBase".equals(type)) {
                    if (restoreKnowledgeBase(id)) {
                        count++;
                    }
                }
            } catch (Exception e) {
                log.error("批量恢复失败: id={}, type={}, error={}", id, type, e.getMessage());
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchPermanentDelete(List<Long> ids, String type) {
        int count = 0;
        for (Long id : ids) {
            try {
                if ("document".equals(type)) {
                    if (permanentDeleteDocument(id)) {
                        count++;
                    }
                } else if ("knowledgeBase".equals(type)) {
                    if (permanentDeleteKnowledgeBase(id)) {
                        count++;
                    }
                }
            } catch (Exception e) {
                log.error("批量永久删除失败: id={}, type={}, error={}", id, type, e.getMessage());
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearRecycleBin() {
        try {
            // 永久删除所有已删除的文档
            List<Document> deletedDocs = documentMapper.selectList(
                    new LambdaQueryWrapper<Document>().eq(Document::getDeleted, 1)
            );
            for (Document doc : deletedDocs) {
                permanentDeleteDocument(doc.getId());
            }

            // 永久删除所有已删除的知识库
            List<KnowledgeBase> deletedKbs = knowledgeBaseMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeBase>().eq(KnowledgeBase::getDeleted, 1)
            );
            for (KnowledgeBase kb : deletedKbs) {
                permanentDeleteKnowledgeBase(kb.getId());
            }

            return true;
        } catch (Exception e) {
            log.error("清空回收站失败: {}", e.getMessage());
            throw new RuntimeException("清空回收站失败: " + e.getMessage());
        }
    }
}
