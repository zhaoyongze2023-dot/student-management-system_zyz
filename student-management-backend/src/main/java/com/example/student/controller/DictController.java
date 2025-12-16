package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dict")
@Slf4j
public class DictController {
    
    @Autowired
    private DictService dictService;
    
    /**
     * 获取班级列表
     */
    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClasses() {
        try {
            List<Map<String, Object>> classes = dictService.getClasses();
            return ResponseEntity.ok(ApiResponse.success(classes));
        } catch (Exception e) {
            log.error("获取班级列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取班级列表失败"));
        }
    }
    
    /**
     * 获取状态字典
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStatus() {
        try {
            List<Map<String, Object>> statusList = dictService.getDictData("status");
            return ResponseEntity.ok(ApiResponse.success(statusList));
        } catch (Exception e) {
            log.error("获取状态字典失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取状态字典失败"));
        }
    }
    
    /**
     * 获取性别字典
     */
    @GetMapping("/gender")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGender() {
        try {
            List<Map<String, Object>> genderList = dictService.getDictData("gender");
            return ResponseEntity.ok(ApiResponse.success(genderList));
        } catch (Exception e) {
            log.error("获取性别字典失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取性别字典失败"));
        }
    }
}
