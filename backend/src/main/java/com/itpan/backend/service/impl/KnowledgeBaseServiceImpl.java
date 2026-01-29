package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.common.enums.KBOwnerType;
import com.itpan.backend.mapper.DocumentMapper;
import com.itpan.backend.mapper.KnowledgeBaseMapper;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;
import com.itpan.backend.service.KnowledgeBaseService;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Resource
    private DocumentMapper documentMapper;

    public IPage<KnowledgeBase> listKnowledgeBases(String scope, String keyword, int pageNum, int pageSize) {
        // 1. 获取当前上下文
        Long currentUserId = UserContext.getUserId();
        Long currentDeptId = UserContext.getDeptId();

        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeBase> query = new LambdaQueryWrapper<>();

        // 2. 通用条件
        query.like(StringUtils.hasText(keyword), KnowledgeBase::getName, keyword);

        // 3. 核心空间路由逻辑
        switch (scope) {
            case "mine": // 【个人空间】
                // 查 owner_type=个人 AND owner_id=我自己
                query.eq(KnowledgeBase::getOwnerType, KBOwnerType.PERSONAL.getCode())
                        .eq(KnowledgeBase::getOwnerId, currentUserId);
                break;

            case "dept": // 【部门空间】
                // 查 owner_type=部门 AND owner_id=我的部门ID
                // (实际项目中可能要查子部门，这里先只查本部门)
                query.eq(KnowledgeBase::getOwnerType, KBOwnerType.DEPARTMENT.getCode())
                        .eq(KnowledgeBase::getOwnerId, currentDeptId);
                break;

            case "public": // 【公共空间】
                // 方案A：只查企业级知识库 (owner_type=30)
                // query.eq(KnowledgeBase::getOwnerType, KBOwnerType.ENTERPRISE.getCode());

                // 方案B (推荐)：查所有 owner_type=30 的 + 其他空间里设为“公开”的
                query.and(wrapper -> wrapper
                        .eq(KnowledgeBase::getOwnerType, KBOwnerType.ENTERPRISE.getCode())
                        .or()
                        .eq(KnowledgeBase::getVisibility, 1) // 只要是公开的，哪里来的都算公共资源
                );
                break;

            default:
                throw new IllegalArgumentException("未知的空间类型");
        }

        query.orderByDesc(KnowledgeBase::getUpdateTime);
        return baseMapper.selectPage(page, query);
    }

    @Override
    public KnowledgeBase getKnowledgeBaseById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean createKnowledgeBase(KnowledgeBase kb, String scope) {
        Long userId = UserContext.getUserId();
        Long deptId = UserContext.getDeptId();

        if ("mine".equals(scope)) {
            kb.setOwnerType(KBOwnerType.PERSONAL.getCode());
            kb.setOwnerId(userId); // 归属个人
        } else if ("dept".equals(scope)) {
            kb.setOwnerType(KBOwnerType.DEPARTMENT.getCode());
            kb.setOwnerId(deptId); // 归属部门
        } else if ("public".equals(scope)) {
            // 只有管理员才能创建公共库，这里加个权限校验
            // checkAdminPermission();
            kb.setOwnerType(KBOwnerType.ENTERPRISE.getCode());
            kb.setOwnerId(0L);     // 归属系统
        }

        return this.save(kb);
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBase knowledgeBase) {
        return baseMapper.updateById(knowledgeBase) > 0;
    }

    @Override
    public boolean deleteKnowledgeBase(Long id) {
        // 【新增】检查该知识库下是否有文档
        Long count = documentMapper.selectCount(
                new LambdaQueryWrapper<Document>().eq(Document::getKbId, id)
        );

        if (count > 0) {
            throw new RuntimeException("该知识库下仍有 " + count + " 个文档，请先清空文档后再删除知识库！");
        }

        // 执行删除
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<Document> getDocuments(Long kbId, String keyword, int pageNum, int pageSize) {
        Page<Document> page = new Page<>(pageNum, pageSize);
        IPage<Document> result = documentMapper.selectPage(page,
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getKbId, kbId)
                        .like(StringUtils.hasText(keyword), Document::getFileName, keyword)
                        .orderByDesc(Document::getCreateTime));
        return result;
    }
}