package com.itpan.backend.controller;

import com.itpan.backend.util.OssUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    @Resource
    private OssUtil ossUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        String url = ossUtil.upload(file.getInputStream(), file.getOriginalFilename());
        return ResponseEntity.ok(url);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam("url") String url) {
        ossUtil.delete(url);
        return ResponseEntity.ok("删除成功");
    }

//    @PostMapping("/download")
//    public ResponseEntity<byte[]> download(@RequestParam("url") String url) {
//        try {
//            java.io.InputStream inputStream = ossUtil.download(url);
//            byte[] bytes = inputStream.readAllBytes();
//            inputStream.close();
//
//            // 获取文件名用于Content-Disposition头
//            String fileName = url.substring(url.lastIndexOf("/") + 1);
//
//            return ResponseEntity.ok()
//                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
//                    .body(bytes);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("url") String url) {
        try {
            java.io.InputStream inputStream = ossUtil.download(url);
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            // 获取文件名用于Content-Disposition头
            String fileName = url.substring(url.lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
