package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itpan.backend.model.entity.DocumentContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档内容Mapper接口
 * 提供文档内容的基本数据库操作
 *
 * @author IT潘
 * @since 2026-02-17
 */
@Mapper
public interface DocumentContentMapper extends BaseMapper<DocumentContent> {

}