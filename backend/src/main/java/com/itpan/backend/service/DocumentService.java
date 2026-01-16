package com.itpan.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document> {
    Document uploadAndSave(MultipartFile file, Long kbId);

    // 根据知识库ID获取文档列表
    List<Document> getListByKbId(Long kbId);

}
