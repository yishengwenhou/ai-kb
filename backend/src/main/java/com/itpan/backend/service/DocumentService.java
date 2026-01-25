package com.itpan.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document> {
    Document uploadAndSave(MultipartFile file, Long kbId, Long parentId);

    // 根据知识库ID获取文档列表
    List<Document> getListByKbId(Long kbId);

    boolean deleteDocument(Long id);

    record PreviewResult(String fileName, byte[] bytes, MediaType mediaType) {
    }

    PreviewResult preview(Long id);

    record downloadResult(String fileName, byte[] bytes) {
    }
    downloadResult download(Long id);

}
