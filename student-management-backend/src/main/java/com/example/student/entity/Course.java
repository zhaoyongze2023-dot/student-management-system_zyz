package com.example.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_teacher_id", columnList = "teacher_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_code", columnList = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "teacher_id")
    private Long teacherId;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "capacity")
    private Integer capacity = 50;
    
    @Column(name = "enrolled")
    private Integer enrolled = 0;
    
    @Column(length = 20)
    private String status = "open";
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "credits")
    private Integer credits;
    
    @Column(length = 200)
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String syllabus;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
