package com.example.student.service;

import com.example.student.dto.PageResponse;
import com.example.student.dto.StudentDTO;
import com.example.student.entity.Class;
import com.example.student.entity.Student;
import com.example.student.repository.ClassRepository;
import com.example.student.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    /**
     * 分页查询学生列表
     */
    @Transactional(readOnly = true)
    public PageResponse<StudentDTO> listStudents(Integer current, Integer size, String keyword, Long classId, String status) {
        // 验证分页参数
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }
        
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 查询学生数据
        Page<Student> page = studentRepository.findByFilters(keyword, classId, status, pageable);
        
        // 转换为 DTO
        List<StudentDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<StudentDTO>builder()
                .total(page.getTotalElements())
                .current(current)
                .size(size)
                .records(records)
                .build();
    }
    
    /**
     * 获取学生详情
     */
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("student not found"));
        
        return convertToDTO(student);
    }
    
    /**
     * 创建学生
     */
    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        // 检查学号是否已存在
        if (studentRepository.existsByStudentId(dto.getStudentId())) {
            throw new RuntimeException("student id already exists");
        }
        
        // 验证班级是否存在
        Class clazz = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("class not found"));
        
        // 创建学生
        Student student = new Student();
        student.setStudentId(dto.getStudentId());
        student.setName(dto.getName());
        student.setClassId(dto.getClassId());
        student.setGender(dto.getGender());
        student.setAge(dto.getAge());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        student.setMajor(dto.getMajor());
        student.setAdmissionYear(dto.getAdmissionYear());
        student.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        
        student = studentRepository.save(student);
        
        // 更新班级学生数
        updateClassStudentCount(dto.getClassId());
        
        log.info("创建学生: {} ({})", student.getName(), student.getStudentId());
        
        return convertToDTO(student);
    }
    
    /**
     * 修改学生
     */
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("student not found"));
        
        // 检查学号是否重复
        if (!student.getStudentId().equals(dto.getStudentId()) && 
            studentRepository.existsByStudentId(dto.getStudentId())) {
            throw new RuntimeException("student id already exists");
        }
        
        // 验证班级
        if (!student.getClassId().equals(dto.getClassId())) {
            classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("class not found"));
        }
        
        // 更新学生信息
        student.setStudentId(dto.getStudentId());
        student.setName(dto.getName());
        student.setClassId(dto.getClassId());
        student.setGender(dto.getGender());
        student.setAge(dto.getAge());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        student.setMajor(dto.getMajor());
        student.setAdmissionYear(dto.getAdmissionYear());
        student.setStatus(dto.getStatus());
        
        if (dto.getAvatarUrl() != null) {
            student.setAvatarUrl(dto.getAvatarUrl());
        }
        
        student = studentRepository.save(student);
        
        log.info("更新学生: {} ({})", student.getName(), student.getStudentId());
        
        return convertToDTO(student);
    }
    
    /**
     * 删除学生
     */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("student not found"));
        
        long classId = student.getClassId();
        
        studentRepository.deleteById(id);
        
        // 更新班级学生数
        updateClassStudentCount(classId);
        
        log.info("删除学生: {} ({})", student.getName(), student.getStudentId());
    }
    
    /**
     * 批量删除学生
     */
    @Transactional
    public void batchDeleteStudents(List<Long> ids) {
        List<Student> students = studentRepository.findByIdIn(ids);
        
        if (students.isEmpty()) {
            return;
        }
        
        // 收集所有受影响的班级ID
        Set<Long> classIds = new HashSet<>();
        for (Student student : students) {
            classIds.add(student.getClassId());
        }
        
        // 删除学生
        studentRepository.deleteAll(students);
        
        // 更新班级学生数
        for (Long classId : classIds) {
            updateClassStudentCount(classId);
        }
        
        log.info("批量删除学生: {}个", students.size());
    }
    
    /**
     * 更新学生头像
     */
    @Transactional
    public void updateStudentAvatar(Long id, String avatarUrl) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("student not found"));
        
        student.setAvatarUrl(avatarUrl);
        studentRepository.save(student);
        
        log.info("更新学生头像: {} ({})", student.getName(), student.getStudentId());
    }
    
    /**
     * 更新班级学生数
     */
    private void updateClassStudentCount(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElse(null);
        
        if (clazz != null) {
            long count = studentRepository.countByClassId(classId);
            clazz.setStudentCount((int) count);
            classRepository.save(clazz);
        }
    }
    
    /**
     * 将 Student 转换为 StudentDTO
     */
    private StudentDTO convertToDTO(Student student) {
        String className = null;
        Optional<Class> classOpt = classRepository.findById(student.getClassId());
        if (classOpt.isPresent()) {
            className = classOpt.get().getName();
        }
        
        return StudentDTO.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .name(student.getName())
                .classId(student.getClassId())
                .className(className)
                .gender(student.getGender())
                .age(student.getAge())
                .phone(student.getPhone())
                .email(student.getEmail())
                .major(student.getMajor())
                .admissionYear(student.getAdmissionYear())
                .status(student.getStatus())
                .avatarUrl(student.getAvatarUrl())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
