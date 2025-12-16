package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.PageResponse;
import com.example.student.dto.StudentDTO;
import com.example.student.service.StudentService;
import com.example.student.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@Slf4j
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private FileUtil fileUtil;
    
    /**
     * 获取学生列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<StudentDTO>>> listStudents(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status) {
        try {
            PageResponse<StudentDTO> pageResponse = studentService.listStudents(current, size, keyword, classId, status);
            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            log.error("获取学生列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取学生列表失败"));
        }
    }
    
    /**
     * 获取学生详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudent(@PathVariable Long id) {
        try {
            StudentDTO student = studentService.getStudentById(id);
            return ResponseEntity.ok(ApiResponse.success(student));
        } catch (RuntimeException e) {
            log.error("获取学生详情失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取学生详情异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取学生详情失败"));
        }
    }
    
    /**
     * 创建学生
     * 只有管理员可以创建学生
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@RequestBody StudentDTO studentDTO) {
        try {
            StudentDTO createdStudent = studentService.createStudent(studentDTO);
            return ResponseEntity.ok(ApiResponse.success("create success", createdStudent));
        } catch (RuntimeException e) {
            log.error("创建学生失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("创建学生异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建学生失败"));
        }
    }
    
    /**
     * 修改学生
     * 只有管理员可以修改学生
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentDTO studentDTO) {
        try {
            StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
            return ResponseEntity.ok(ApiResponse.success("update success", updatedStudent));
        } catch (RuntimeException e) {
            log.error("更新学生失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新学生异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新学生失败"));
        }
    }
    
    /**
     * 删除学生
     * 只有管理员可以删除学生
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(ApiResponse.success("delete success", null));
        } catch (RuntimeException e) {
            log.error("删除学生失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("删除学生异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除学生失败"));
        }
    }
    
    /**
     * 批需删除学生
     * 只有管理员可以批需删除
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Object>> batchDeleteStudents(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "ids不能为空"));
            }
            
            studentService.batchDeleteStudents(ids);
            return ResponseEntity.ok(ApiResponse.success("batch delete success", null));
        } catch (Exception e) {
            log.error("批量删除学生异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "批量删除学生失败"));
        }
    }
}
