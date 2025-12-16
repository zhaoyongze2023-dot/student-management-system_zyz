package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.service.StudentService;
import com.example.student.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {
    
    @Autowired
    private FileUtil fileUtil;
    
    @Autowired
    private StudentService studentService;
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "文件为空"));
            }
            
            // 保存文件
            String url = fileUtil.saveAvatar(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            
            return ResponseEntity.ok(ApiResponse.success("upload success", result));
        } catch (IllegalArgumentException e) {
            log.error("文件验证失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "文件验证失败: " + e.getMessage()));
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传异常"));
        }
    }
    
    /**
     * 上传其他文件
     */
    @PostMapping("/file")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "files") String directory) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "文件为空"));
            }
            
            // 保存文件
            String url = fileUtil.saveFile(file, directory);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            
            return ResponseEntity.ok(ApiResponse.success("upload success", result));
        } catch (IllegalArgumentException e) {
            log.error("文件验证失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "文件验证失败: " + e.getMessage()));
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传异常"));
        }
    }
    
    /**
     * 为学生更新头像
     */
    @PostMapping("/student/{id}/avatar")
    public ResponseEntity<ApiResponse<Object>> updateStudentAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "文件为空"));
            }
            
            // 保存文件
            String url = fileUtil.saveAvatar(file);
            
            // 更新学生头像
            studentService.updateStudentAvatar(id, url);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            
            return ResponseEntity.ok(ApiResponse.success("upload success", result));
        } catch (IllegalArgumentException e) {
            log.error("文件验证失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "文件验证失败: " + e.getMessage()));
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传异常"));
        }
    }
}
