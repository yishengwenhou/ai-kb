package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.model.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 查询已删除的文档列表
     */
    IPage<Document> selectDeletedPage(Page<Document> page, @Param("keyword") String keyword);

    /**
     * 永久删除（物理删除）
     */
    int permanentDelete(@Param("id") Long id);
}