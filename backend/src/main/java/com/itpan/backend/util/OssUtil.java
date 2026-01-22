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
    public String getPrivateUrl(String objectName, int expirationSeconds) {
        // 如果传进来的是完整URL，先提取出 ObjectName (你现有的方法)
        if (objectName.startsWith("http")) {
            objectName = extractObjectNameFromUrl(objectName);
        }

        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000L);
        // 生成签名URL
        java.net.URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, expiration);

        return url.toString();
    }

    public String upload(InputStream inputStream, String fileName) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf("."));
            String objectName = "uploads/" +
                    new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) +
                    UUID.randomUUID().toString().replace("-", "") + ext;

            PutObjectRequest request = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    inputStream);

            ossClient.putObject(request);
            return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
        } catch (OSSException e) {
            log.error("OSS上传失败 - 错误码: {}, 错误信息: {}", e.getErrorCode(), e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getErrorCode() + " - " + e.getMessage(), e);
        } catch (ClientException e) {
            log.error("OSS客户端错误 - 请求ID: {}, 错误信息: {}", e.getRequestId(), e.getMessage());
            throw new RuntimeException("文件上传失败: 客户端错误 - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("OSS上传发生未知错误", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    public void delete(String url) {
        try {
            // 从URL中提取对象名称，移除域名部分
            String objectName = extractObjectNameFromUrl(url);
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
        } catch (OSSException e) {
            log.error("OSS删除失败 - 错误码: {}, 错误信息: {}", e.getErrorCode(), e.getMessage());
            throw new RuntimeException("文件删除失败: " + e.getErrorCode() + " - " + e.getMessage(), e);
        } catch (ClientException e) {
            log.error("OSS客户端错误 - 请求ID: {}, 错误信息: {}", e.getRequestId(), e.getMessage());
            throw new RuntimeException("文件删除失败: 客户端错误 - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("OSS删除发生未知错误", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
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

    public InputStream download(String url) {
        try {
            // 从URL中提取对象名称，移除域名部分
            String objectName = extractObjectNameFromUrl(url);
            return ossClient.getObject(ossConfig.getBucketName(), objectName).getObjectContent();
        } catch (OSSException e) {
            log.error("OSS下载失败 - 错误码: {}, 错误信息: {}", e.getErrorCode(), e.getMessage());
            throw new RuntimeException("文件下载失败: " + e.getErrorCode() + " - " + e.getMessage(), e);
        } catch (ClientException e) {
            log.error("OSS客户端错误 - 请求ID: {}, 错误信息: {}", e.getRequestId(), e.getMessage());
            throw new RuntimeException("文件下载失败: 客户端错误 - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("OSS下载发生未知错误", e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }
}
