package com.example.student.repository;

import com.example.student.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    
    @Query("SELECT c FROM Course c WHERE (c.name LIKE %:keyword% OR c.code LIKE %:keyword% OR c.description LIKE %:keyword%) AND c.status = :status")
    Page<Course> findByCourseByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.name LIKE %:keyword% OR c.code LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Course> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    Page<Course> findByStatus(String status, Pageable pageable);
    
    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);
}
