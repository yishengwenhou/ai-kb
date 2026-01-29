package com.itpan.backend.controller;

import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.DocumentParser;
import com.itpan.backend.util.OssUtil;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Resource
    private UserService userService;

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

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "只能上传图片文件");
                return ResponseEntity.badRequest().body(error);
            }

            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "图片大小不能超过 2MB");
                return ResponseEntity.badRequest().body(error);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "avatar/" + UserContext.getCurrentUser().getId() + "/" + UUID.randomUUID() + extension;

            // 上传到 OSS
            String url = ossUtil.upload(fileName, file.getInputStream());

            // 更新用户头像
            User currentUser = UserContext.getCurrentUser();
            User user = userService.getById(currentUser.getId());
            if (user != null) {
                user.setAvatarUrl(url);
                userService.updateById(user);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}