package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.dto.knowledgeBase.KnowledgeBaseCreateDTO;
import com.itpan.backend.model.dto.knowledgeBase.KnowledgeBaseUpdateDTO;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;

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
     * 从DTO创建知识库
     * @param createDTO 创建DTO
     * @param scope 空间范围
     * @return 创建的知识库实体
     */
    KnowledgeBase createKnowledgeBaseFromDTO(KnowledgeBaseCreateDTO createDTO, String scope);

    /**
     * 更新知识库
     * @param knowledgeBase 知识库实体
     * @return 是否更新成功
     */
    boolean updateKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * 从DTO更新知识库
     * @param updateDTO 更新DTO
     * @return 是否更新成功
     */
    boolean updateKnowledgeBaseFromDTO(KnowledgeBaseUpdateDTO updateDTO);

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
//    IPage<Document> getDocuments(Long kbId, String keyword, int pageNum, int pageSize);
}