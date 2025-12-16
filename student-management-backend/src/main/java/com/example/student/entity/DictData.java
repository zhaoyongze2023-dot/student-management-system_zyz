package com.example.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dict_data", indexes = {
    @Index(name = "idx_dict_type", columnList = "dict_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String label;
    
    @Column(nullable = false, length = 100)
    private String value;
    
    @Column(nullable = false, length = 50)
    private String dictType;
    
    private Integer sort = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
