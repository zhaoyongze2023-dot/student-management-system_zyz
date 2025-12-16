package com.example.student.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import io.minio.messages.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@Slf4j
public class FileUtil {
    
    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${upload.max-file-size:10485760}")
    private long maxFileSize;
    
    @Value("${upload.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,xlsx,xls}")
    private String allowedExtensions;
    
    @Value("${minio.enabled:false}")
    private boolean minioEnabled;
    
    @Value("${minio.bucket:student-management}")
    private String minioBucket;
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;
    
    @Autowired(required = false)
    private MinioClient minioClient;
    
    /**
     * 保存头像文件
     */
    public String saveAvatar(MultipartFile file) throws IOException {
        // 验证文件
        validateFile(file);
        
        String avatarDir = "avatars";
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID() + "." + fileExtension;
        
        if (minioEnabled && minioClient != null) {
            // 上传到 Minio
            String objectName = avatarDir + "/" + fileName;
            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioBucket)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
                log.info("头像已上传到 Minio: {}/{}", minioBucket, objectName);
                return generateMinioUrl(objectName);
            } catch (Exception e) {
                log.error("上传到 Minio 失败，回退到本地存储", e);
                return saveAvatarLocal(avatarDir, fileName, file);
            }
        } else {
            // 保存到本地
            return saveAvatarLocal(avatarDir, fileName, file);
        }
    }
    
    /**
     * 保存头像到本地文件系统
     */
    private String saveAvatarLocal(String avatarDir, String fileName, MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(uploadPath, avatarDir);
        Files.createDirectories(uploadDir);
        
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        log.info("文件已保存到本地: {}", filePath);
        return "/uploads/" + avatarDir + "/" + fileName;
    }
    
    /**
     * 保存其他文件（课程附件等）
     */
    public String saveFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID() + "." + fileExtension;
        
        if (minioEnabled && minioClient != null) {
            // 上传到 Minio
            String objectName = directory + "/" + fileName;
            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioBucket)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
                log.info("文件已上传到 Minio: {}/{}", minioBucket, objectName);
                return generateMinioUrl(objectName);
            } catch (Exception e) {
                log.error("上传到 Minio 失败，回退到本地存储", e);
                return saveFileLocal(directory, fileName, file);
            }
        } else {
            // 保存到本地
            return saveFileLocal(directory, fileName, file);
        }
    }
    
    /**
     * 保存文件到本地文件系统
     */
    private String saveFileLocal(String directory, String fileName, MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(uploadPath, directory);
        Files.createDirectories(uploadDir);
        
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        log.info("文件已保存到本地: {}", filePath);
        return "/uploads/" + directory + "/" + fileName;
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            if (minioEnabled && minioClient != null && isMinioUrl(filePath)) {
                // 从 Minio 删除
                String objectName = extractObjectNameFromUrl(filePath);
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(minioBucket)
                                    .object(objectName)
                                    .build()
                    );
                    log.info("文件已从 Minio 删除: {}/{}", minioBucket, objectName);
                    return true;
                } catch (Exception e) {
                    log.error("从 Minio 删除文件失败", e);
                    return false;
                }
            } else {
                // 从本地删除
                String localPath = filePath.replace("/uploads/", "");
                Path path = Paths.get(uploadPath, localPath);
                
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("文件已从本地删除: {}", path);
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("删除文件失败: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 生成 Minio URL
     */
    private String generateMinioUrl(String objectName) {
        // 返回完整的 Minio 访问 URL
        // 格式: http://localhost:9000/bucket/object
        return minioEndpoint + "/" + minioBucket + "/" + objectName;
    }
    
    /**
     * 判断 URL 是否是 Minio URL
     */
    private boolean isMinioUrl(String url) {
        return url != null && url.startsWith(minioEndpoint);
    }
    
    /**
     * 从 URL 提取对象名称
     */
    private String extractObjectNameFromUrl(String url) {
        // 从 URL 中提取对象名称
        // 如: http://localhost:9000/bucket/avatars/file.jpg -> avatars/file.jpg
        String prefix = minioEndpoint + "/" + minioBucket + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return url;
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + maxFileSize);
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        Set<String> extensions = new HashSet<>(Arrays.asList(allowedExtensions.toLowerCase().split(",")));
        
        if (!extensions.contains(fileExtension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名为空");
        }
        
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("文件没有扩展名");
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
}
