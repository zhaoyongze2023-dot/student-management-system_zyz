package com.example.student.service;

import com.example.student.dto.CourseDTO;
import com.example.student.dto.StudentDTO;
import com.example.student.entity.Course;
import com.example.student.entity.Student;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    
    /**
     * 搜索课程 - 使用数据库LIKE查询（高效）
     * 查询: name LIKE ?% OR code LIKE ?% OR description LIKE ?%
     */
    public Page<CourseDTO> searchCourses(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseRepository.findAll(pageable)
                    .map(this::convertCourseToDTO);
        }
        
        return courseRepository.findByKeyword(keyword, pageable)
                .map(this::convertCourseToDTO);
    }
    
    /**
     * 搜索学生 - 使用数据库LIKE查询（高效）
     * 查询: name LIKE ?% OR studentId LIKE ?% OR email LIKE ?% OR phone LIKE ?%
     */
    public Page<StudentDTO> searchStudents(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll(pageable)
                    .map(this::convertStudentToDTO);
        }
        
        return studentRepository.findByKeyword(keyword, pageable)
                .map(this::convertStudentToDTO);
    }
    
    /**
     * 全局搜索（课程+学生） - 使用数据库LIKE查询（高效）
     */
    public SearchResultDTO globalSearch(String keyword, Pageable pageable) {
        SearchResultDTO result = new SearchResultDTO();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }
        
        // 搜索课程 - 数据库查询
        Page<CourseDTO> coursePage = searchCourses(keyword, pageable);
        result.setCourses(coursePage.getContent());
        result.setCourseTotal(coursePage.getTotalElements());
        
        // 搜索学生 - 数据库查询
        Page<StudentDTO> studentPage = searchStudents(keyword, pageable);
        result.setStudents(studentPage.getContent());
        result.setStudentTotal(studentPage.getTotalElements());
        
        result.setTotal(coursePage.getTotalElements() + studentPage.getTotalElements());
        
        return result;
    }
    
    /**
     * 获取热门搜索关键词（基于课程）
     */
    public List<String> getPopularKeywords(int limit) {
        try {
            List<Course> courses = courseRepository.findAll();
            return courses.stream()
                    .filter(course -> course != null && course.getName() != null && !course.getName().trim().isEmpty())
                    .map(Course::getName)
                    .distinct()
                    .limit(Math.max(1, limit))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Course DTO转换
     */
    private CourseDTO convertCourseToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        dto.setTeacherId(course.getTeacherId());
        dto.setCategory(course.getCategory());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolled(course.getEnrolled());
        dto.setStatus(course.getStatus());
        dto.setCredits(course.getCredits());
        dto.setLocation(course.getLocation());
        return dto;
    }
    
    /**
     * Student DTO转换
     */
    private StudentDTO convertStudentToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setStudentId(student.getStudentId());
        return dto;
    }
    
    /**
     * 搜索结果DTO
     */
    public static class SearchResultDTO {
        private List<CourseDTO> courses = new ArrayList<>();
        private Long courseTotal = 0L;
        private List<StudentDTO> students = new ArrayList<>();
        private Long studentTotal = 0L;
        private Long total = 0L;
        
        public List<CourseDTO> getCourses() {
            return courses;
        }
        
        public void setCourses(List<CourseDTO> courses) {
            this.courses = courses;
        }
        
        public Long getCourseTotal() {
            return courseTotal;
        }
        
        public void setCourseTotal(Long courseTotal) {
            this.courseTotal = courseTotal;
        }
        
        public List<StudentDTO> getStudents() {
            return students;
        }
        
        public void setStudents(List<StudentDTO> students) {
            this.students = students;
        }
        
        public Long getStudentTotal() {
            return studentTotal;
        }
        
        public void setStudentTotal(Long studentTotal) {
            this.studentTotal = studentTotal;
        }
        
        public Long getTotal() {
            return total;
        }
        
        public void setTotal(Long total) {
            this.total = total;
        }
    }
}
