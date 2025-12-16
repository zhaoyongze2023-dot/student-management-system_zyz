package com.example.student.controller;

import com.example.student.dto.*;
import com.example.student.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@Slf4j
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 获取课程列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<CourseDTO>>> listCourses(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String semester) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<CourseDTO> page = courseService.listCourses(keyword, status, pageable);
            
            PageResponse<CourseDTO> response = PageResponse.<CourseDTO>builder()
                    .current(current)
                    .size(size)
                    .total(page.getTotalElements())
                    .records(page.getContent())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取课程列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取课程列表失败"));
        }
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseById(@PathVariable Long id) {
        try {
            CourseDTO course = courseService.getCourseById(id);
            return ResponseEntity.ok(ApiResponse.success(course));
        } catch (RuntimeException e) {
            log.error("获取课程详情失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取课程详情异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取课程详情失败"));
        }
    }
    
    /**
     * 创建课程
     * 只有教师和管理员可以创建课程
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(@RequestBody CourseDTO courseDTO) {
        try {
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            return ResponseEntity.ok(ApiResponse.success("课程创建成功", createdCourse));
        } catch (RuntimeException e) {
            log.error("创建课程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("创建课程异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建课程失败"));
        }
    }
    
    /**
     * 更新课程
     * 只有教师和管理员可以更新课程
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO courseDTO) {
        try {
            CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
            return ResponseEntity.ok(ApiResponse.success("课程更新成功", updatedCourse));
        } catch (RuntimeException e) {
            log.error("更新课程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("更新课程异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新课程失败"));
        }
    }
    
    /**
     * 删除课程
     * 只有管理员可以删除课程
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponse.success("课程删除成功", null));
        } catch (RuntimeException e) {
            log.error("删除课程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("删除课程异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除课程失败"));
        }
    }
    
    /**
     * 添加课程日程
     * 只有教师和管理员可以添加课程日程
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/{courseId}/schedules")
    public ResponseEntity<ApiResponse<CourseScheduleDTO>> addSchedule(
            @PathVariable Long courseId,
            @RequestBody CourseScheduleDTO scheduleDTO) {
        try {
            CourseScheduleDTO schedule = courseService.addCourseSchedule(courseId, scheduleDTO);
            return ResponseEntity.ok(ApiResponse.success("课程日程添加成功", schedule));
        } catch (RuntimeException e) {
            log.error("添加课程日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("添加课程日程异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加课程日程失败"));
        }
    }
    
    /**
     * 删除课程日程
     * 只有教师和管理员可以删除课程日程
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<Object>> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            courseService.deleteCourseSchedule(scheduleId);
            return ResponseEntity.ok(ApiResponse.success("课程日程删除成功", null));
        } catch (RuntimeException e) {
            log.error("删除课程日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("删除课程日程异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除课程日程失败"));
        }
    }
    
    /**
     * 添加课程附件
     */
    @PostMapping("/{courseId}/attachments")
    public ResponseEntity<ApiResponse<CourseAttachmentDTO>> addAttachment(
            @PathVariable Long courseId,
            @RequestBody CourseAttachmentDTO attachmentDTO) {
        try {
            CourseAttachmentDTO attachment = courseService.addCourseAttachment(courseId, attachmentDTO);
            return ResponseEntity.ok(ApiResponse.success("课程附件添加成功", attachment));
        } catch (RuntimeException e) {
            log.error("添加课程附件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("添加课程附件异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加课程附件失败"));
        }
    }
    
    /**
     * 删除课程附件
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Object>> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            courseService.deleteCourseAttachment(attachmentId);
            return ResponseEntity.ok(ApiResponse.success("课程附件删除成功", null));
        } catch (RuntimeException e) {
            log.error("删除课程附件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("删除课程附件异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除课程附件失败"));
        }
    }
}
