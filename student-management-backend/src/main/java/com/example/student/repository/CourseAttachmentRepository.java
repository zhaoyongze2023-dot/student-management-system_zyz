package com.example.student.repository;

import com.example.student.entity.CourseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseAttachmentRepository extends JpaRepository<CourseAttachment, Long> {
    List<CourseAttachment> findByCourseId(Long courseId);
}
