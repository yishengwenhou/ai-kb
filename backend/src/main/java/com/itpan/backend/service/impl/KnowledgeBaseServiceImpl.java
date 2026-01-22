package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    @Override
    public List<KnowledgeBase> listKnowledgeBases(String keyword, Long deptId, int pageNum, int pageSize) {

        Long userDeptId = UserContext.getDeptId();
        if(!userDeptId.equals(deptId)) return List.of();

        Long userId = UserContext.getUserId();

        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        IPage<KnowledgeBase> result = baseMapper.selectPage(page,
                new LambdaQueryWrapper<KnowledgeBase>()
                        .like(StringUtils.hasText(keyword), KnowledgeBase::getName, keyword)
                        .eq(KnowledgeBase::getVisibility,1)
                        .or(i->i.eq(KnowledgeBase::getVisibility,0).eq(KnowledgeBase::getCreateBy, userId))
                        .or(i->i.eq(KnowledgeBase::getVisibility,2).eq(KnowledgeBase::getDeptId, deptId))
                        .orderByDesc(KnowledgeBase::getId)
        );
        
        return result.getRecords();
    }

    @Override
    public KnowledgeBase getKnowledgeBaseById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean createKnowledgeBase(KnowledgeBase knowledgeBase) {
        return baseMapper.insert(knowledgeBase) > 0;
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
    public List<Document> getDocuments(Long kbId,String keyword) {
        List<Document> documents = documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getKbId, kbId)
                        .like(StringUtils.hasText(keyword),Document::getFileName,keyword)
                        .orderByDesc(Document::getCreateTime)
        );
        return documents;
    }
}