package com.itpan.backend.controller;

import com.itpan.backend.model.entity.Document;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.service.impl.DocumentAsyncServiceImpl;
import com.itpan.backend.util.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;


@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAsyncServiceImpl documentAsyncService;

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
            Document document = documentService.uploadAndSave(file, kbId, parentId);
            documentAsyncService.parseDocument(document.getId());
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        }
    }


    /**
     * 下载文档
     * 优化：参数改为 id，通过 ID 查 URL，防止恶意用户随便传 URL 下载非本文档的文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws UnsupportedEncodingException {

        DocumentService.downloadResult doc = documentService.download(id);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" +
                        java.net.URLEncoder.encode(doc.fileName(), "UTF-8") + "\"")
                .body(doc.bytes());
    }

    /**
     * 预览解析后的文本内容 (调试或前端展示用)
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<?> getContent(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        // 如果内容为空，可能是还没解析完
        if (doc.getContent() == null) {
            return ResponseEntity.ok("文档正在解析中或解析失败...");
        }
        return ResponseEntity.ok(doc.getContent());
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) throws UnsupportedEncodingException {

        DocumentService.PreviewResult result = documentService.preview(id);

        return ResponseEntity.ok()
                .contentType(result.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                        java.net.URLEncoder.encode(result.fileName(), "UTF-8") + "\"")
                .body(result.bytes());
    }


    /**
     * 重试解析
     * 迁移自 ResourceController.retryParse
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryParse(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        // 重置状态
        doc.setStatus(1); // 处理中
        documentService.updateById(doc);

        // 触发异步解析
        documentAsyncService.parseDocument(id);

        return ResponseEntity.ok("已提交重试任务");
    }

    /**
     * 删除文档 (级联删除)
     * 建议：在 Service 层实现 deleteDocument 方法，包含删除数据库记录和OSS文件
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // 调用 Service 层的完整逻辑
        boolean success = documentService.deleteDocument(id);
        if (success) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}