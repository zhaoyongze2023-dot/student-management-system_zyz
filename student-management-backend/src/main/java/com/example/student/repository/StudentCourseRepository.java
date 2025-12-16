package com.example.student.repository;

import com.example.student.entity.StudentCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<StudentCourse> findByStudentId(Long studentId);
    
    List<StudentCourse> findByStudentIdAndStatus(Long studentId, String status);
    
    List<StudentCourse> findByCourseId(Long courseId);
    
    Page<StudentCourse> findByStudentIdAndStatus(Long studentId, String status, Pageable pageable);
    
    @Query("SELECT sc FROM StudentCourse sc WHERE sc.studentId = :studentId AND sc.status = 'active'")
    List<StudentCourse> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);
    
    Integer countByCourseIdAndStatus(Long courseId, String status);
}
