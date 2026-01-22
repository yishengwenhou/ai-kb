package com.itpan.backend.controller;

import com.itpan.backend.util.DocumentParser;
import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ⚠️ 仅用于开发调试，生产环境建议禁用或移除
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Resource
    private OssUtil ossUtil;

    @Resource
    private DocumentParser documentParser;

    /**
     * 测试工具：给定任意OSS URL，测试能不能解析出文字
     */
    @GetMapping("/test-parse")
    public ResponseEntity<String> testParse(@RequestParam("url") String url) {
        try {
            java.io.InputStream inputStream = ossUtil.download(url);
            String content = documentParser.parse(inputStream);
            inputStream.close();
            return ResponseEntity.ok("解析成功 (长度" + content.length() + "):\n" + content);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("解析失败: " + e.getMessage());
        }
    }
}