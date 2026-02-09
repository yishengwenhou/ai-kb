package com.itpan.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.dto.document.DocContentUpdateDTO;
import com.itpan.backend.model.dto.document.DocCreateDTO;
import com.itpan.backend.model.dto.document.MoveNodeDTO;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.vo.DocumentVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document> {

    List<DocumentVO> getChildren(Long kbId, Long parentId);

    Document createNode(DocCreateDTO docCreateDTO);

    void moveNode(MoveNodeDTO moveNodeDTO);

    Document uploadFile(MultipartFile file, Long kbId, Long parentId);

    DocumentVO getNodeDetail(Long id);

    boolean updateContent(DocContentUpdateDTO docContentUpdateDTO);

    List<DocumentVO> getBreadcrumb(Long id);

    void downloadFile(Long id, HttpServletResponse response);

    void removeNode(Long id);


}
