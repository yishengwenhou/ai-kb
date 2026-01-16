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
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Resource
    private DocumentMapper documentMapper;

    @Override
    public List<KnowledgeBase> listKnowledgeBases(String name, int pageNum, int pageSize) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(KnowledgeBase::getName, name);
        }
        wrapper.orderByDesc(KnowledgeBase::getId);
        
        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        IPage<KnowledgeBase> result = baseMapper.selectPage(page, wrapper);
        
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
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public List<Document> getDocuments(Long kbId) {
        List<Document> documents = documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getKbId, kbId)
        );
        return documents;
    }
}