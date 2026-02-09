package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.dto.RecycleBinItem;
import com.itpan.backend.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回收站控制器
 */
@RestController
@RequestMapping("/api/recycle")
@RequiredArgsConstructor
public class


RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 分页查询回收站列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getRecycleBinList(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        IPage<RecycleBinItem> result = recycleBinService.getRecycleBinList(type, keyword, pageNum, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    /**
     * 恢复文档
     */
    @PostMapping("/restore/document/{id}")
    public ResponseEntity<?> restoreDocument(@PathVariable Long id) {
        try {
            boolean success = recycleBinService.restoreDocument(id);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "文档恢复成功");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "文档恢复失败", "success", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    /**
     * 恢复知识库
     */
    @PostMapping("/restore/kb/{id}")
    public ResponseEntity<?> restoreKnowledgeBase(@PathVariable Long id) {
        try {
            boolean success = recycleBinService.restoreKnowledgeBase(id);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "知识库恢复成功");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "知识库恢复失败", "success", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    /**
     * 永久删除文档
     */
    @DeleteMapping("/permanent/document/{id}")
    public ResponseEntity<?> permanentDeleteDocument(@PathVariable Long id) {
        try {
            boolean success = recycleBinService.permanentDeleteDocument(id);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "文档已永久删除");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "永久删除失败", "success", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    /**
     * 永久删除知识库
     */
    @DeleteMapping("/permanent/kb/{id}")
    public ResponseEntity<?> permanentDeleteKnowledgeBase(@PathVariable Long id) {
        try {
            boolean success = recycleBinService.permanentDeleteKnowledgeBase(id);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "知识库已永久删除");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "永久删除失败", "success", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    /**
     * 批量恢复
     * 请求体: {"ids": [1,2,3], "type": "document"}
     */
    @PostMapping("/batch/restore")
    public ResponseEntity<?> batchRestore(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        String type = (String) request.get("type");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID列表不能为空", "success", false));
        }

        if (!"document".equals(type) && !"knowledgeBase".equals(type)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "类型参数错误", "success", false));
        }

        int count = recycleBinService.batchRestore(ids, type);

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("成功恢复 %d 项", count));
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 批量永久删除
     * 请求体: {"ids": [1,2,3], "type": "document"}
     */
    @PostMapping("/batch/delete")
    public ResponseEntity<?> batchPermanentDelete(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        String type = (String) request.get("type");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID列表不能为空", "success", false));
        }

        if (!"document".equals(type) && !"knowledgeBase".equals(type)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "类型参数错误", "success", false));
        }

        int count = recycleBinService.batchPermanentDelete(ids, type);

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("成功永久删除 %d 项", count));
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearRecycleBin() {
        try {
            boolean success = recycleBinService.clearRecycleBin();

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "回收站已清空");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "清空回收站失败", "success", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }
}
