package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itpan.backend.model.entity.Document;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

}