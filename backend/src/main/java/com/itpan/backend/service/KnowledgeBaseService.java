package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;
import java.util.List;

public interface KnowledgeBaseService extends IService<KnowledgeBase>{

    IPage<KnowledgeBase> listKnowledgeBases(String scope, String keyword, int pageNum, int pageSize);

    /**
     * 根据ID获取知识库详情
     * @param id 知识库ID
     * @return 知识库实体
     */
    KnowledgeBase getKnowledgeBaseById(Long id);


    boolean createKnowledgeBase(KnowledgeBase kb, String scope);

    /**
     * 更新知识库
     * @param knowledgeBase 知识库实体
     * @return 是否更新成功
     */
    boolean updateKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * 删除知识库
     * @param id 知识库ID
     * @return 是否删除成功
     */
    boolean deleteKnowledgeBase(Long id);

    /**
     * 获取知识库下的文档列表
     * @param kbId 知识库ID
     * @return 文档列表
     */
    IPage<Document> getDocuments(Long kbId, String keyword, int pageNum, int pageSize);
}