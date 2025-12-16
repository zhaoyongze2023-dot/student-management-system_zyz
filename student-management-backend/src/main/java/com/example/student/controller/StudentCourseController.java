package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.PageResponse;
import com.example.student.dto.StudentCourseDTO;
import com.example.student.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/student/courses", "/student-course"})
@RequiredArgsConstructor
public class StudentCourseController {
    private final StudentCourseService studentCourseService;
    
    /**
     * 学生选课
     * 仅学生可以选课
     * 文档要求：POST /api/student-course/enroll，支持 JSON Body 或查询参数，分页参数 page/pageSize
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollCourse(@RequestBody(required = false) EnrollRequest body,
                                          @RequestParam(value = "courseId", required = false) Long courseIdParam,
                                          @RequestParam(value = "studentId", required = false) Long studentIdParam) {
        try {
            Long courseId = courseIdParam != null ? courseIdParam : (body != null ? body.getCourseId() : null);
            if (courseId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "courseId 必填"));
            }

            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }

            StudentCourseDTO dto = studentCourseService.enrollCourse(studentId, courseId);
            return ResponseEntity.ok(ApiResponse.success("选课成功", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "选课失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生退课（文档：DELETE /api/student-course/{enrollmentId}），保留旧的 /drop 兼容
     * 仅学生可以退课
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/drop")
    public ResponseEntity<?> dropCourseCompat(@RequestParam Long studentId, @RequestParam Long courseId) {
        try {
            studentCourseService.dropCourseByStudentAndCourse(studentId, courseId);
            return ResponseEntity.ok(ApiResponse.success("退课成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "退课失败: " + e.getMessage()));
        }
    }

    /**
     * 学生退课
     * 仅学生可以退课
     */
    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<?> dropCourse(@PathVariable Long enrollmentId,
                                        @RequestParam(value = "studentId", required = false) Long studentIdParam) {
        try {
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            studentCourseService.dropCourseByEnrollmentId(enrollmentId, studentId);
            return ResponseEntity.ok(ApiResponse.success("退课成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "退课失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取学生已选课程列表
     */
    @GetMapping("/enrolled")
    public ResponseEntity<?> getEnrolledCourses(
            @RequestParam(value = "studentId", required = false) Long studentIdParam,
            @RequestParam(value = "status", defaultValue = "active") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "current", required = false) Integer currentCompat,
            @RequestParam(value = "size", required = false) Integer sizeCompat) {
        try {
            int pageIndex = currentCompat != null ? currentCompat : page;
            int pageSizeVal = sizeCompat != null ? sizeCompat : pageSize;
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }
            Pageable pageable = PageRequest.of(Math.max(0, pageIndex - 1), pageSizeVal);
            Page<StudentCourseDTO> result = studentCourseService.getEnrolledCourses(studentId, status, pageable);

            return ResponseEntity.ok(ApiResponse.success("success", buildPage(result, pageIndex, pageSizeVal)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取已选课程失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取学生活跃选课列表
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCourses(@RequestParam(value = "studentId", required = false) Long studentIdParam) {
        try {
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }
            List<StudentCourseDTO> dtos = studentCourseService.getActiveEnrollments(studentId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", dtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取活跃课程失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取可选课程列表
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCourses(
            @RequestParam(value = "studentId", required = false) Long studentIdParam,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "current", required = false) Integer currentCompat,
            @RequestParam(value = "size", required = false) Integer sizeCompat) {
        try {
            int pageIndex = currentCompat != null ? currentCompat : page;
            int pageSizeVal = sizeCompat != null ? sizeCompat : pageSize;
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }
            Pageable pageable = PageRequest.of(Math.max(0, pageIndex - 1), pageSizeVal);
            Page<StudentCourseDTO> result = studentCourseService.getAvailableCourses(studentId, pageable);

            return ResponseEntity.ok(ApiResponse.success("success", buildPage(result, pageIndex, pageSizeVal)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取可选课程失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取选课历史
     */
    @GetMapping("/history")
    public ResponseEntity<?> getEnrollmentHistory(
            @RequestParam(value = "studentId", required = false) Long studentIdParam,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "current", required = false) Integer currentCompat,
            @RequestParam(value = "size", required = false) Integer sizeCompat) {
        try {
            int pageIndex = currentCompat != null ? currentCompat : page;
            int pageSizeVal = sizeCompat != null ? sizeCompat : pageSize;
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }
            Pageable pageable = PageRequest.of(Math.max(0, pageIndex - 1), pageSizeVal);
            Page<StudentCourseDTO> result = studentCourseService.getEnrollmentHistory(studentId, pageable);

            return ResponseEntity.ok(ApiResponse.success("success", buildPage(result, pageIndex, pageSizeVal)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取选课历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取选课详情
     */
    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<?> getEnrollmentDetail(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            StudentCourseDTO dto = studentCourseService.getEnrollmentDetail(studentId, courseId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取选课详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 教师打成绩
     */
    @PostMapping("/{studentId}/{courseId}/grade")
    public ResponseEntity<?> gradeStudent(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam String grade) {
        try {
            studentCourseService.gradeStudent(studentId, courseId, grade);
            return ResponseEntity.ok(ApiResponse.success("成绩录入成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "成绩录入失败: " + e.getMessage()));
        }
    }
    
    /**
     * 完成课程
     */
    @PostMapping("/{studentId}/{courseId}/complete")
    public ResponseEntity<?> completeCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            studentCourseService.completeCourse(studentId, courseId);
            return ResponseEntity.ok(ApiResponse.success("课程标记为已完成", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "完成课程失败: " + e.getMessage()));
        }
    }

    /**
     * 导出选课清单（简化实现：返回 CSV 文本流）
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportEnrollments(
            @RequestParam(value = "studentId", required = false) Long studentIdParam,
            @RequestParam(value = "format", defaultValue = "csv") String format) {
        try {
            Long studentId = studentCourseService.resolveStudentId(studentIdParam);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "studentId 缺失，且无法从登录信息解析"));
            }
            String csv = studentCourseService.exportEnrollmentsCsv(studentId);
            String filename = "enrollments.csv";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(csv);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "导出失败: " + e.getMessage()));
        }
    }

    private static <T> PageResponse<T> buildPage(Page<T> page, int pageIndex, int pageSize) {
        return new PageResponse<>(page.getTotalElements(), pageIndex, pageSize, page.getContent());
    }

    @lombok.Data
    private static class EnrollRequest {
        private Long courseId;
        private String remark;
    }
}
