package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.dto.RecycleBinItem;

import java.util.List;

/**
 * 回收站服务接口
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站
     * @param type 类型: all-全部, document-文档, knowledgeBase-知识库
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 回收站项列表
     */
    IPage<RecycleBinItem> getRecycleBinList(String type, String keyword, int pageNum, int pageSize);

    /**
     * 恢复文档
     * @param id 文档ID
     * @return 是否成功
     */
    boolean restoreDocument(Long id);

    /**
     * 恢复知识库
     * @param id 知识库ID
     * @return 是否成功
     */
    boolean restoreKnowledgeBase(Long id);

    /**
     * 永久删除文档
     * @param id 文档ID
     * @return 是否成功
     */
    boolean permanentDeleteDocument(Long id);

    /**
     * 永久删除知识库
     * @param id 知识库ID
     * @return 是否成功
     */
    boolean permanentDeleteKnowledgeBase(Long id);

    /**
     * 批量恢复
     * @param ids ID列表
     * @param type 类型: document-文档, knowledgeBase-知识库
     * @return 成功恢复的数量
     */
    int batchRestore(List<Long> ids, String type);

    /**
     * 批量永久删除
     * @param ids ID列表
     * @param type 类型: document-文档, knowledgeBase-知识库
     * @return 成功删除的数量
     */
    int batchPermanentDelete(List<Long> ids, String type);

    /**
     * 清空回收站
     * @return 是否成功
     */
    boolean clearRecycleBin();
}
