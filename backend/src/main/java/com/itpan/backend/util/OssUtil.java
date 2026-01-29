package com.itpan.backend.util;

import com.aliyun.oss.*;
import com.aliyun.oss.model.PutObjectRequest;
import com.itpan.backend.config.OssConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class OssUtil {
    @Resource
    private OssConfig ossConfig;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());
    }

    /**
     * 生成带签名的临时访问链接 (针对私有读 Bucket)
     * @param objectName OSS中的文件名 (如 uploads/2023/...) 或 完整URL
     * @param expirationSeconds 过期时间(秒)，例如 3600 代表1小时
     * @return 带签名的URL
     */
//    public String getPrivateUrl(String objectName, int expirationSeconds) {
//        // 如果传进来的是完整URL，先提取出 ObjectName (你现有的方法)
//        if (objectName.startsWith("http")) {
//            objectName = extractObjectNameFromUrl(objectName);
//        }
//
//        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000L);
//        // 生成签名URL
//        java.net.URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, expiration);
//
//        return url.toString();
//    }

    public String upload(String bucketName, InputStream inputStream, String fileName) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf("."));
            String objectName = "uploads/" +
                    new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) +
                    UUID.randomUUID().toString().replace("-", "") + ext;

            PutObjectRequest request = new PutObjectRequest(
                    bucketName, // 使用传入的 bucket
                    objectName,
                    inputStream);

            ossClient.putObject(request);
            return "https://" + bucketName + "." + ossConfig.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            log.error("OSS上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    public void delete(String fileUrl) {
        try {
            String bucketName = getBucketNameFromUrl(fileUrl);
            String objectName = getObjectNameFromUrl(fileUrl);
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            log.error("OSS删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 从URL中提取对象名称，移除域名部分
     * @param url 完整的文件URL
     * @return 对象名称（路径+文件名）
     */
    private String extractObjectNameFromUrl(String url) {
        // 假设URL格式为: https://bucketname.endpoint/ObjectName
        // 需要提取 / 之后的部分作为对象名称
        int firstSlashIndex = url.indexOf("/", url.indexOf("://") + 3); // 找到协议（http://或https://）之后的第一个slash
        if (firstSlashIndex != -1) {
            return url.substring(firstSlashIndex + 1); // 返回slash之后的部分
        }
        // 如果没有找到预期的格式，直接返回最后一部分（向后兼容）
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public InputStream download(String fileUrl) {
        try {
            String bucketName = getBucketNameFromUrl(fileUrl);
            String objectName = getObjectNameFromUrl(fileUrl);
            return ossClient.getObject(bucketName, objectName).getObjectContent();
        } catch (Exception e) {
            log.error("OSS下载失败", e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    private String getBucketNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String host = url.getHost();
            return host.substring(0, host.indexOf("."));
        } catch (Exception e) {
            throw new IllegalArgumentException("无法从URL解析Bucket名称: " + fileUrl);
        }
    }

    private String getObjectNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            return url.getPath().substring(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法从URL解析对象名称: " + fileUrl);
        }
    }
}
