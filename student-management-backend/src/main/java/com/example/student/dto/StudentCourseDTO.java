package com.example.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCourseDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    
    @JsonProperty("courseName")
    private String courseName;
    
    @JsonProperty("courseCode")
    private String courseCode;
    
    @JsonProperty("teacherName")
    private String teacherName;
    
    @JsonProperty("credits")
    private Integer credits;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    @JsonProperty("enrolled")
    private Integer enrolled;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("grade")
    private String grade;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("enrollDate")
    private LocalDateTime enrollDate;
    
    @JsonProperty("schedules")
    private List<?> schedules;
    
    @JsonProperty("attachments")
    private List<?> attachments;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
