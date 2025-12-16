package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.CourseDTO;
import com.example.student.dto.PageResponse;
import com.example.student.dto.StudentDTO;
import com.example.student.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    
    /**
     * 搜索课程
     */
    @GetMapping("/courses")
    public ResponseEntity<?> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<CourseDTO> page = searchService.searchCourses(keyword, pageable);
            
            PageResponse<CourseDTO> response = new PageResponse<CourseDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("搜索成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "课程搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索学生
     */
    @GetMapping("/students")
    public ResponseEntity<?> searchStudents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<StudentDTO> page = searchService.searchStudents(keyword, pageable);
            
            PageResponse<StudentDTO> response = new PageResponse<StudentDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("搜索成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "学生搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 全局搜索
     */
    @GetMapping("/global")
    public ResponseEntity<?> globalSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            SearchService.SearchResultDTO result = searchService.globalSearch(keyword, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("搜索成功", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "全局搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/popular-keywords")
    public ResponseEntity<?> getPopularKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> keywords = searchService.getPopularKeywords(limit);
            return ResponseEntity.ok(ApiResponse.success("获取成功", keywords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取热门关键词失败: " + e.getMessage()));
        }
    }
}
