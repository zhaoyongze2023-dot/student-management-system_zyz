package com.example.student.service;

import com.example.student.dto.StudentCourseDTO;
import com.example.student.entity.Course;
import com.example.student.entity.Student;
import com.example.student.entity.StudentCourse;
import com.example.student.entity.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.StudentCourseRepository;
import com.example.student.repository.StudentRepository;
import com.example.student.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentCourseService {
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    
    /**
     * 优先使用显式 studentId；否则尝试从登录用户名映射到学生学号；失败则返回 null。
     */
    public Long resolveStudentId(Long studentIdParam) {
        if (studentIdParam != null) {
            return studentIdParam;
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        String username = auth.getName();
        return studentRepository.findByStudentId(username)
                .map(Student::getId)
                .orElse(null);
    }
    
    /**
     * 学生选课
     */
    public StudentCourseDTO enrollCourse(Long studentId, Long courseId) {
        // 检查学生是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("学生不存在");
        }
        
        // 检查课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        
        // 检查学生是否已选过该课程
        Optional<StudentCourse> existing = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent() && "active".equals(existing.get().getStatus())) {
            throw new IllegalArgumentException("学生已选过该课程");
        }
        
        // 检查课程容量
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new IllegalArgumentException("课程已满，无法选择");
        }
        
        // 创建选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        studentCourse.setEnrollDate(LocalDateTime.now());
        studentCourse.setStatus("active");
        
        StudentCourse saved = studentCourseRepository.save(studentCourse);
        
        // 更新课程已选人数
        course.setEnrolled(course.getEnrolled() + 1);
        courseRepository.save(course);
        
        return convertToDTO(saved, course);
    }
    
    /**
     * 学生退课
     */
    public void dropCourseByStudentAndCourse(Long studentId, Long courseId) {
        StudentCourse studentCourse = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        dropInternal(studentCourse);
    }

    public void dropCourseByEnrollmentId(Long enrollmentId, Long studentId) {
        StudentCourse studentCourse = studentCourseRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        if (studentId != null && !studentId.equals(studentCourse.getStudentId())) {
            throw new IllegalArgumentException("无权操作该选课记录");
        }
        dropInternal(studentCourse);
    }

    private void dropInternal(StudentCourse studentCourse) {
        if (!"active".equals(studentCourse.getStatus())) {
            throw new IllegalArgumentException("只能退出活跃的选课");
        }
        // 直接删除选课记录（而不是标记为 dropped）
        studentCourseRepository.delete(studentCourse);
        
        // 更新课程的已选人数
        Course course = courseRepository.findById(studentCourse.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        course.setEnrolled(Math.max(0, course.getEnrolled() - 1));
        courseRepository.save(course);
    }
    
    /**
     * 获取学生已选课程列表
     */
    public Page<StudentCourseDTO> getEnrolledCourses(Long studentId, String status, Pageable pageable) {
        Page<StudentCourse> page = studentCourseRepository.findByStudentIdAndStatus(studentId, status, pageable);
        return page.map(sc -> {
            Course course = courseRepository.findById(sc.getCourseId()).orElse(null);
            return convertToDTO(sc, course);
        });
    }
    
    /**
     * 获取学生活跃选课列表（不分页）
     */
    public List<StudentCourseDTO> getActiveEnrollments(Long studentId) {
        List<StudentCourse> enrollments = studentCourseRepository.findActiveEnrollmentsByStudentId(studentId);
        return enrollments.stream()
                .map(sc -> {
                    Course course = courseRepository.findById(sc.getCourseId()).orElse(null);
                    return convertToDTO(sc, course);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取可选课程列表（学生未选过的课程）
     * 修复：先获取所有可用课程，再进行分页
     */
    public Page<StudentCourseDTO> getAvailableCourses(Long studentId, Pageable pageable) {
        // 获取学生已选的课程ID（只包括活跃的选课，不包括已退课和已完成的）
        List<Long> enrolledCourseIds = studentCourseRepository.findByStudentId(studentId)
                .stream()
                .filter(sc -> "active".equals(sc.getStatus()))
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
        
        // 获取所有开放课程（不分页）
        List<Course> allCourses = courseRepository.findAll();
        
        // 过滤出可选课程（学生未选且课程状态为 open）
        List<StudentCourseDTO> availableList = allCourses.stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()) && "open".equals(course.getStatus()))
                .map(this::convertCourseToDTO)
                .collect(Collectors.toList());
        
        // 手动分页：根据 pageable 的 pageNumber 和 pageSize 进行分页
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, availableList.size());
        
        // 确保 start 不超过可用列表大小，避免 IndexOutOfBoundsException
        if (start >= availableList.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, availableList.size());
        }
        
        List<StudentCourseDTO> paged = availableList.subList(start, end);
        
        return new PageImpl<>(paged, pageable, availableList.size());
    }
    
    /**
     * 将课程实体转换为选课 DTO（用于可选课程列表）
     */
    private StudentCourseDTO convertCourseToDTO(Course course) {
        StudentCourseDTO dto = new StudentCourseDTO();
        dto.setId(course.getId());
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getName());
        dto.setCourseCode(course.getCode());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolled(course.getEnrolled());
        dto.setLocation(course.getLocation());
        dto.setCredits(course.getCredits());
        dto.setStatus(course.getStatus());
        
        // 获取教师信息
        if (course.getTeacherId() != null) {
            User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
            dto.setTeacherName(teacher != null ? teacher.getUsername() : "N/A");
        } else {
            dto.setTeacherName("N/A");
        }
        
        // 初始化附件和时间表为空数组（而不是 null）
        dto.setSchedules(Collections.emptyList());
        dto.setAttachments(Collections.emptyList());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * 获取学生选课历史（所有状态）
     */
    public Page<StudentCourseDTO> getEnrollmentHistory(Long studentId, Pageable pageable) {
        List<StudentCourse> allEnrollments = studentCourseRepository.findByStudentId(studentId);
        List<StudentCourseDTO> dtos = allEnrollments.stream()
                .map(sc -> {
                    Course course = courseRepository.findById(sc.getCourseId()).orElse(null);
                    return convertToDTO(sc, course);
                })
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        if (start >= dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        List<StudentCourseDTO> pageContent = dtos.subList(start, end);
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    /**
     * 获取选课详情
     */
    public StudentCourseDTO getEnrollmentDetail(Long studentId, Long courseId) {
        StudentCourse studentCourse = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        
        return convertToDTO(studentCourse, course);
    }
    
    /**
     * 打成绩
     */
    public void gradeStudent(Long studentId, Long courseId, String grade) {
        StudentCourse studentCourse = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        
        studentCourse.setGrade(grade);
        studentCourseRepository.save(studentCourse);
    }
    
    /**
     * 完成课程
     */
    public void completeCourse(Long studentId, Long courseId) {
        StudentCourse studentCourse = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        
        studentCourse.setStatus("completed");
        studentCourseRepository.save(studentCourse);
    }

    public String exportEnrollmentsCsv(Long studentId) {
        List<StudentCourseDTO> data = getEnrolledCourses(studentId, "active", Pageable.unpaged()).getContent();
        StringBuilder sb = new StringBuilder();
        sb.append("id,courseId,courseName,status,enrollDate\n");
        for (StudentCourseDTO dto : data) {
            sb.append(dto.getId()).append(',')
                    .append(dto.getCourseId()).append(',')
                    .append(safe(dto.getCourseName())).append(',')
                    .append(safe(dto.getStatus())).append(',')
                    .append(dto.getEnrollDate()).append('\n');
        }
        return sb.toString();
    }
    
    private String safe(String v) {
        return v == null ? "" : v.replace(',', ' ');
    }
    
    /**
     * DTO转换助手
     */
    private StudentCourseDTO convertToDTO(StudentCourse studentCourse, Course course) {
        StudentCourseDTO dto = new StudentCourseDTO();
        dto.setId(studentCourse.getId());
        dto.setStudentId(studentCourse.getStudentId());
        dto.setCourseId(studentCourse.getCourseId());
        dto.setStatus(studentCourse.getStatus());
        dto.setGrade(studentCourse.getGrade());
        dto.setEnrollDate(studentCourse.getEnrollDate());
        dto.setCreatedAt(studentCourse.getCreatedAt());
        dto.setUpdatedAt(studentCourse.getUpdatedAt());
        
        if (course != null) {
            dto.setCourseName(course.getName());
            dto.setCourseCode(course.getCode());
            dto.setCapacity(course.getCapacity());
            dto.setEnrolled(course.getEnrolled());
            dto.setCredits(course.getCredits());
            dto.setLocation(course.getLocation());
            
            // 获取教师名称
            if (course.getTeacherId() != null) {
                userRepository.findById(course.getTeacherId())
                        .ifPresent(user -> dto.setTeacherName(user.getUsername()));
            }
        }
        
        return dto;
    }
}

