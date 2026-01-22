package com.itpan.backend.controller;

import com.itpan.backend.model.entity.Document;
import com.itpan.backend.service.DocumentService;
import com.itpan.backend.service.impl.DocumentAsyncServiceImpl;
import com.itpan.backend.util.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAsyncServiceImpl documentAsyncService;
    private final OssUtil ossUtil;

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
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        try {
            java.io.InputStream inputStream = ossUtil.download(doc.getFilePath());
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            // 设置响应头，强制浏览器下载
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" +
                            java.net.URLEncoder.encode(doc.getFileName(), "UTF-8") + "\"")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        try {
            // 1. 下载文件流
            java.io.InputStream inputStream = ossUtil.download(doc.getFilePath());
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            // 2. 猜测文件类型 (MIME Type)
            String ext = doc.getFileType().toLowerCase();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            // 根据后缀设置正确的 MIME，这样浏览器才知道怎么渲染
            if ("pdf".equals(ext)) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if ("png".equals(ext)) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("txt".equals(ext)) {
                mediaType = MediaType.TEXT_PLAIN;
            }

            // 3. 返回响应 (inline 表示在线显示)
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                            java.net.URLEncoder.encode(doc.getFileName(), "UTF-8") + "\"")
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
            // 如果 Service 返回 false，说明 ID 不存在，返回 404 是合理的
            return ResponseEntity.notFound().build();
        }
    }
}