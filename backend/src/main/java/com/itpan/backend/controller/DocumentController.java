package com.itpan.backend.controller;

import com.itpan.backend.model.dto.document.DocContentUpdateDTO;
import com.itpan.backend.model.dto.document.DocCreateDTO;
import com.itpan.backend.model.dto.document.MoveNodeDTO;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.vo.DocumentVO;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.service.impl.DocumentAsyncServiceImpl;
import com.itpan.backend.util.OssUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.List;


@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAsyncServiceImpl documentAsyncService;

    // 1. 获取列表 (飞书模式：懒加载，点哪层查哪层)
    // 前端点开一个文件夹，调用一次这个接口，传 parentId
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam Long kbId, @RequestParam(defaultValue = "0") Long parentId) {
        List<DocumentVO> list = documentService.getChildren(kbId, parentId);
        return ResponseEntity.ok(list);
    }

    // 2. 新建文档/文件夹 (统一接口)
    // 前端传 type="doc" 就是建文档，传 type="folder" 就是建文件夹
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody DocCreateDTO docCreateDTO) {
        Document document = documentService.createNode(docCreateDTO);
        return ResponseEntity.ok(document);
    }

    // 3. 移动/排序 (飞书体验核心)
    // 当用户拖拽文档改变顺序或层级时调用
    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody MoveNodeDTO moveNodeDTO) {
        // 逻辑：更新 parentId, treePath 和 sort
        documentService.moveNode(moveNodeDTO);
        return ResponseEntity.ok("移动成功");
    }

    /**
     * 上传文档到指定知识库
     * 迁移自 ResourceController.upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("kbId") Long kbId,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            Document document = documentService.uploadFile(file, kbId, parentId);
            documentAsyncService.parseDocument(document.getId());
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        }
    }

    // DocumentController.java

    /**
     * 1. 获取文档详情 (用于编辑回显)
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<DocumentVO> getDetail(@PathVariable Long id) {
        DocumentVO vo = documentService.getNodeDetail(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * 2. 更新文档内容 (对接编辑器自动保存)
     */
    @PutMapping("/content")
    public ResponseEntity<?> updateContent(@RequestBody @Valid DocContentUpdateDTO docContentUpdateDTO) {
        documentService.updateContent(docContentUpdateDTO);
        return ResponseEntity.ok("保存成功");
    }

    /**
     * 3. 重命名或更换图标
     */
//    @PutMapping("/meta")
//    public ResponseEntity<?> updateMeta(@RequestBody @Valid DocMetaUpdateDTO dto) {
//        documentService.updateMeta(dto);
//        return ResponseEntity.ok("更新成功");
//    }

    /**
     * 4. 获取面包屑导航 (利用 tree_path 高效查询)
     */
    @GetMapping("/breadcrumb/{id}")
    public ResponseEntity<List<DocumentVO>> getBreadcrumb(@PathVariable Long id) {
        List<DocumentVO> list = documentService.getBreadcrumb(id);
        return ResponseEntity.ok(list);
    }

    /**
     * 5. 文件下载
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        documentService.downloadFile(id, response);
    }

    /**
     * 6. 逻辑删除 (移入回收站)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNode(@PathVariable Long id) {
        documentService.removeNode(id);
        return ResponseEntity.ok("已移入回收站");
    }
}