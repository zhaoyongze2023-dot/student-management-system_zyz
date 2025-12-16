package com.example.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_attachments", indexes = {
    @Index(name = "idx_course_id_attachment", columnList = "course_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(name = "size")
    private Long size;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}
