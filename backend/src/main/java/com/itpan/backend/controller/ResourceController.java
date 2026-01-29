package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itpan.backend.common.constants.OssConstant;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.DocumentParser;
import com.itpan.backend.util.OssUtil;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
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
            // 1. 基础校验 (保持不变)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "只能上传图片文件"));
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "图片大小不能超过 2MB"));
            }

            // 2. 【核心】计算文件哈希 (MD5)
            String fileHash = DigestUtils.md5DigestAsHex(file.getInputStream());

            // 3. 【核心】去重查询：查 User 表看有没有人用过这张图
            User existUser = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getAvatarHash, fileHash)
                    .isNotNull(User::getAvatarUrl)
                    .last("LIMIT 1")); // 只需找到一个就行

            String url;
            if (existUser != null) {
                url = existUser.getAvatarUrl();
            } else {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String fileName = "avatar/" + UserContext.getUserId() + "/" + UUID.randomUUID() + extension;

                url = ossUtil.upload(OssConstant.BUCKET_AVATAR, file.getInputStream(), fileName);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", url);
            response.put("hash", fileHash);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

//    // 修改 ResourceController.java
//    @PostMapping("/avatar")
//    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
//        try {
//            // ... 校验文件大小和类型的代码保持不变 ...
//
//            // 1. 上传到 OSS
//            String fileName = "avatar/" + UserContext.getUserId() + "/" + UUID.randomUUID() + extension;
//            String url = ossUtil.upload(file.getInputStream(), fileName);
//
//            // 2. 【关键修改】这里不要去调用 userService.updateById(user) 了！
//            // 仅仅返回 URL 给前端即可，让前端决定什么时候保存
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("data", url); // 只返回 URL
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            // ... 异常处理 ...
//        }
//    }
}