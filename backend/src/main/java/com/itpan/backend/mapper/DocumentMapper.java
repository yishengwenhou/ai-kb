package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.vo.DocumentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


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

    @Select("SELECT MAX(sort) FROM document WHERE kb_id = #{kbId} AND parent_id = #{parentId} AND deleted = 0")
    Double selectMaxSort(@Param("kbId") Long kbId, @Param("parentId") Long parentId);

    void updateTreePathByParent(@Param("oldPathPrefix") String oldPathPrefix,
                                @Param("newPathPrefix") String newPathPrefix,
                                @Param("kbId") Long kbId);

    List<DocumentVO> selectNodeList(@Param("kbId") Long kbId, @Param("parentId") Long parentId);
}