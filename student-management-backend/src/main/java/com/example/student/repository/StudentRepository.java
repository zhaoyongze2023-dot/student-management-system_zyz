package com.example.student.repository;

import com.example.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    
    boolean existsByStudentId(String studentId);
    
    @Query("SELECT s FROM Student s WHERE " +
           "(:keyword IS NULL OR s.name LIKE %:keyword% OR s.studentId LIKE %:keyword%) AND " +
           "(:classId IS NULL OR s.classId = :classId) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Student> findByFilters(
        @Param("keyword") String keyword,
        @Param("classId") Long classId,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.studentId LIKE %:keyword% OR s.email LIKE %:keyword% OR s.phone LIKE %:keyword%")
    Page<Student> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<Student> findByClassId(Long classId);
    
    long countByClassId(Long classId);
    
    List<Student> findByIdIn(List<Long> ids);
}
