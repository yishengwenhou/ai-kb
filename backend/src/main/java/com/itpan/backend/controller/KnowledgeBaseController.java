package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.entity.KnowledgeBase;
import com.itpan.backend.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 分页查询知识库列表
     */
    @GetMapping
    public ResponseEntity<?> listKnowledgeBases(
            @RequestParam(required = false) String keyword,
            @RequestParam Long deptId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.listKnowledgeBases(keyword, deptId, pageNum, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", knowledgeBases);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取知识库详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgeBase(@PathVariable Long id) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getKnowledgeBaseById(id);
        
        if (knowledgeBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "知识库不存在", "success", false));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", knowledgeBase);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取知识库下的文档列表
     * @param id 知识库ID
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return
     */
    @GetMapping("/{id}/documents")
    public ResponseEntity<IPage<Document>> getDocuments(@PathVariable Long id,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Document> documents = knowledgeBaseService.getDocuments(id,keyword,pageNum, pageSize);
        return ResponseEntity.ok(documents);
    }


    /**
     * 创建知识库
     */
    @PostMapping
    public ResponseEntity<?> createKnowledgeBase(@RequestBody KnowledgeBase knowledgeBase) {
        // 设置初始状态值
        if (knowledgeBase.getStatus() == null) {
            knowledgeBase.setStatus(0); // 默认就绪状态
        }
        if (knowledgeBase.getVisibility() == null) {
            knowledgeBase.setVisibility(0); // 默认私有
        }
        
        boolean success = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", knowledgeBase);
            response.put("message", "创建成功");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "创建失败", "success", false));
        }
    }

    /**
     * 更新知识库
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKnowledgeBase(@PathVariable Long id, @RequestBody KnowledgeBase knowledgeBase) {
        // 确保ID匹配
        knowledgeBase.setId(id);
        
        boolean success = knowledgeBaseService.updateKnowledgeBase(knowledgeBase);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", knowledgeBase);
            response.put("message", "更新成功");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "更新失败", "success", false));
        }
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKnowledgeBase(@PathVariable Long id) {
        boolean success = knowledgeBaseService.deleteKnowledgeBase(id);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "删除成功");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "删除失败", "success", false));
        }
    }
}