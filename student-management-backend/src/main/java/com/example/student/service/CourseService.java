package com.example.student.service;

import com.example.student.dto.CourseDTO;
import com.example.student.dto.CourseScheduleDTO;
import com.example.student.dto.CourseAttachmentDTO;
import com.example.student.entity.Course;
import com.example.student.entity.CourseSchedule;
import com.example.student.entity.CourseAttachment;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.CourseScheduleRepository;
import com.example.student.repository.CourseAttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseScheduleRepository scheduleRepository;
    
    @Autowired
    private CourseAttachmentRepository attachmentRepository;
    
    /**
     * 获取课程列表
     */
    @Transactional(readOnly = true)
    public Page<CourseDTO> listCourses(String keyword, String status, Pageable pageable) {
        Page<Course> coursePage;
        
        if (keyword != null && !keyword.isEmpty() && status != null && !status.isEmpty()) {
            coursePage = courseRepository.findByCourseByKeywordAndStatus(keyword, status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            coursePage = courseRepository.findByKeyword(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            coursePage = courseRepository.findByStatus(status, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }
        
        return coursePage.map(this::convertToDTO);
    }
    
    /**
     * 获取课程详情
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course not found");
        }
        
        Course course = courseOpt.get();
        CourseDTO dto = convertToDTO(course);
        
        // 加载日程和附件
        dto.setSchedules(scheduleRepository.findByCourseId(courseId).stream()
                .map(this::convertScheduleToDTO)
                .collect(Collectors.toList()));
        
        dto.setAttachments(attachmentRepository.findByCourseId(courseId).stream()
                .map(this::convertAttachmentToDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * 创建课程
     */
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        // 检查课程代码是否唯一
        if (courseRepository.findByCode(courseDTO.getCode()).isPresent()) {
            throw new RuntimeException("Course code already exists");
        }
        
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setCode(courseDTO.getCode());
        course.setDescription(courseDTO.getDescription());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setCategory(courseDTO.getCategory());
        course.setCapacity(courseDTO.getCapacity() != null ? courseDTO.getCapacity() : 50);
        course.setStatus(courseDTO.getStatus() != null ? courseDTO.getStatus() : "open");
        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());
        course.setCredits(courseDTO.getCredits());
        course.setLocation(courseDTO.getLocation());
        course.setSyllabus(courseDTO.getSyllabus());
        course.setRequirements(courseDTO.getRequirements());
        course.setEnrolled(0);
        
        course = courseRepository.save(course);
        log.info("课程创建成功: {}", course.getName());
        
        return convertToDTO(course);
    }
    
    /**
     * 更新课程
     */
    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseDTO courseDTO) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course not found");
        }
        
        Course course = courseOpt.get();
        
        if (courseDTO.getName() != null) course.setName(courseDTO.getName());
        if (courseDTO.getDescription() != null) course.setDescription(courseDTO.getDescription());
        if (courseDTO.getTeacherId() != null) course.setTeacherId(courseDTO.getTeacherId());
        if (courseDTO.getCategory() != null) course.setCategory(courseDTO.getCategory());
        if (courseDTO.getCapacity() != null) course.setCapacity(courseDTO.getCapacity());
        if (courseDTO.getStatus() != null) course.setStatus(courseDTO.getStatus());
        if (courseDTO.getStartDate() != null) course.setStartDate(courseDTO.getStartDate());
        if (courseDTO.getEndDate() != null) course.setEndDate(courseDTO.getEndDate());
        if (courseDTO.getCredits() != null) course.setCredits(courseDTO.getCredits());
        if (courseDTO.getLocation() != null) course.setLocation(courseDTO.getLocation());
        if (courseDTO.getSyllabus() != null) course.setSyllabus(courseDTO.getSyllabus());
        if (courseDTO.getRequirements() != null) course.setRequirements(courseDTO.getRequirements());
        
        course = courseRepository.save(course);
        log.info("课程更新成功: {}", course.getName());
        
        return convertToDTO(course);
    }
    
    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }
        
        // 删除相关的日程和附件
        scheduleRepository.deleteAll(scheduleRepository.findByCourseId(courseId));
        attachmentRepository.deleteAll(attachmentRepository.findByCourseId(courseId));
        
        courseRepository.deleteById(courseId);
        log.info("课程删除成功: {}", courseId);
    }
    
    /**
     * 添加课程日程
     */
    @Transactional
    public CourseScheduleDTO addCourseSchedule(Long courseId, CourseScheduleDTO scheduleDTO) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }
        
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(courseId);
        schedule.setDayOfWeek(scheduleDTO.getDayOfWeek());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setLocation(scheduleDTO.getLocation());
        
        schedule = scheduleRepository.save(schedule);
        log.info("课程日程添加成功: courseId={}", courseId);
        
        return convertScheduleToDTO(schedule);
    }
    
    /**
     * 删除课程日程
     */
    @Transactional
    public void deleteCourseSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Schedule not found");
        }
        
        scheduleRepository.deleteById(scheduleId);
        log.info("课程日程删除成功: {}", scheduleId);
    }
    
    /**
     * 添加课程附件
     */
    @Transactional
    public CourseAttachmentDTO addCourseAttachment(Long courseId, CourseAttachmentDTO attachmentDTO) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }
        
        CourseAttachment attachment = new CourseAttachment();
        attachment.setCourseId(courseId);
        attachment.setName(attachmentDTO.getName());
        attachment.setUrl(attachmentDTO.getUrl());
        attachment.setSize(attachmentDTO.getSize());
        
        attachment = attachmentRepository.save(attachment);
        log.info("课程附件添加成功: courseId={}, filename={}", courseId, attachmentDTO.getName());
        
        return convertAttachmentToDTO(attachment);
    }
    
    /**
     * 删除课程附件
     */
    @Transactional
    public void deleteCourseAttachment(Long attachmentId) {
        if (!attachmentRepository.existsById(attachmentId)) {
            throw new RuntimeException("Attachment not found");
        }
        
        attachmentRepository.deleteById(attachmentId);
        log.info("课程附件删除成功: {}", attachmentId);
    }
    
    /**
     * 将 Course 转换为 CourseDTO
     */
    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .category(course.getCategory())
                .capacity(course.getCapacity())
                .enrolled(course.getEnrolled())
                .status(course.getStatus())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .credits(course.getCredits())
                .location(course.getLocation())
                .syllabus(course.getSyllabus())
                .requirements(course.getRequirements())
                .schedules(java.util.Collections.emptyList())
                .attachments(java.util.Collections.emptyList())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
    
    private CourseScheduleDTO convertScheduleToDTO(CourseSchedule schedule) {
        return CourseScheduleDTO.builder()
                .id(schedule.getId())
                .courseId(schedule.getCourseId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
    
    private CourseAttachmentDTO convertAttachmentToDTO(CourseAttachment attachment) {
        return CourseAttachmentDTO.builder()
                .id(attachment.getId())
                .courseId(attachment.getCourseId())
                .name(attachment.getName())
                .url(attachment.getUrl())
                .size(attachment.getSize())
                .uploadedAt(attachment.getUploadedAt())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
