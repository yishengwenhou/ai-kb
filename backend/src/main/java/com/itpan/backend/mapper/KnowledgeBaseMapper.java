package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 查询已删除的知识库列表
     */
    IPage<KnowledgeBase> selectDeletedPage(Page<KnowledgeBase> page, @Param("keyword") String keyword);

    /**
     * 永久删除（物理删除）
     */
    int permanentDelete(@Param("id") Long id);
}