package com.itpan.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;
import java.util.List;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {
    /**
     * 分页查询知识库列表
     * @param keyword 知识库名称（可选，用于模糊查询）
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 知识库列表
     */
    List<KnowledgeBase> listKnowledgeBases(String keyword, Long deptId, int pageNum, int pageSize);

    /**
     * 根据ID获取知识库详情
     * @param id 知识库ID
     * @return 知识库实体
     */
    KnowledgeBase getKnowledgeBaseById(Long id);

    /**
     * 创建知识库
     * @param knowledgeBase 知识库实体
     * @return 是否创建成功
     */
    boolean createKnowledgeBase(KnowledgeBase knowledgeBase);

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
    List<Document> getDocuments(Long kbId,String keyword);
}