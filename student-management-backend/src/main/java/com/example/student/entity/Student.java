package com.example.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_class_id", columnList = "class_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String studentId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private Long classId;
    
    @Column(length = 1)
    private String gender;
    
    private Integer age;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 100)
    private String major;
    
    private Integer admissionYear;
    
    @Column(length = 20, nullable = false)
    private String status = "active";
    
    @Column(length = 255)
    private String avatarUrl;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Transient
    private String className;
}
