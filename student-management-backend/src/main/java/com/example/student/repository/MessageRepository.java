package com.example.student.repository;

import com.example.student.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverId(Long receiverId);
    
    Page<Message> findByReceiverId(Long receiverId, Pageable pageable);
    
    List<Message> findByReceiverIdAndStatus(Long receiverId, String status);
    
    Page<Message> findByReceiverIdAndStatus(Long receiverId, String status, Pageable pageable);
    
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    
    Page<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId AND m.status = 'unread'")
    Integer countUnreadMessages(@Param("receiverId") Long receiverId);
    
    @Query("SELECT m FROM Message m WHERE m.receiverId = :receiverId ORDER BY m.createdAt DESC")
    List<Message> findLatestMessages(@Param("receiverId") Long receiverId);
}
