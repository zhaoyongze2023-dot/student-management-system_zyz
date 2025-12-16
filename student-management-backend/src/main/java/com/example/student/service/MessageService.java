package com.example.student.service;

import com.example.student.dto.MessageDTO;
import com.example.student.entity.Message;
import com.example.student.entity.User;
import com.example.student.repository.MessageRepository;
import com.example.student.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    /**
     * 发送消息
     */
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        // 检查发送者和接收者
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("发送者不存在"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("接收者不存在"));
        
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setStatus("unread");
        
        Message saved = messageRepository.save(message);
        return convertToDTO(saved, sender, receiver);
    }
    
    /**
     * 获取收件箱消息（分页）
     */
    public Page<MessageDTO> getInboxMessages(Long receiverId, Pageable pageable) {
        Page<Message> page = messageRepository.findByReceiverId(receiverId, pageable);
        return page.map(msg -> {
            User sender = msg.getSenderId() != null ? userRepository.findById(msg.getSenderId()).orElse(null) : null;
            User receiver = msg.getReceiverId() != null ? userRepository.findById(msg.getReceiverId()).orElse(null) : null;
            return convertToDTO(msg, sender, receiver);
        });
    }
    
    /**
     * 获取未读消息
     */
    public Page<MessageDTO> getUnreadMessages(Long receiverId, Pageable pageable) {
        Page<Message> page = messageRepository.findByReceiverIdAndStatus(receiverId, "unread", pageable);
        return page.map(msg -> {
            User sender = msg.getSenderId() != null ? userRepository.findById(msg.getSenderId()).orElse(null) : null;
            User receiver = msg.getReceiverId() != null ? userRepository.findById(msg.getReceiverId()).orElse(null) : null;
            return convertToDTO(msg, sender, receiver);
        });
    }
    
    /**
     * 获取消息会话（与某用户的对话）
     */
    public Page<MessageDTO> getConversation(Long userId1, Long userId2, Pageable pageable) {
        Page<Message> page = messageRepository.findBySenderIdAndReceiverId(userId1, userId2, pageable);
        return page.map(msg -> {
            User sender = msg.getSenderId() != null ? userRepository.findById(msg.getSenderId()).orElse(null) : null;
            User receiver = msg.getReceiverId() != null ? userRepository.findById(msg.getReceiverId()).orElse(null) : null;
            return convertToDTO(msg, sender, receiver);
        });
    }
    
    /**
     * 标记消息为已读（验证消息属于指定用户）
     */
    public void markAsRead(Long messageId, Long receiverId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        
        // 验证消息属于当前用户
        if (!message.getReceiverId().equals(receiverId)) {
            throw new IllegalArgumentException("无权限操作该消息");
        }
        
        message.setStatus("read");
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
    }
    
    /**
     * 标记多条消息为已读
     */
    public void markMultipleAsRead(Long receiverId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndStatus(receiverId, "unread");
        LocalDateTime now = LocalDateTime.now();
        
        for (Message msg : unreadMessages) {
            msg.setStatus("read");
            msg.setReadAt(now);
        }
        
        messageRepository.saveAll(unreadMessages);
    }
    
    /**
     * 获取未读消息数量
     */
    public Integer getUnreadCount(Long receiverId) {
        return messageRepository.countUnreadMessages(receiverId);
    }
    
    /**
     * 删除消息
     */
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        messageRepository.delete(message);
    }
    
    /**
     * 获取最新消息
     */
    public List<MessageDTO> getLatestMessages(Long receiverId, int limit) {
        List<Message> messages = messageRepository.findLatestMessages(receiverId);
        return messages.stream()
                .limit(limit)
                .map(msg -> {
                    User sender = msg.getSenderId() != null ? userRepository.findById(msg.getSenderId()).orElse(null) : null;
                    User receiver = msg.getReceiverId() != null ? userRepository.findById(msg.getReceiverId()).orElse(null) : null;
                    return convertToDTO(msg, sender, receiver);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * DTO转换
     */
    private MessageDTO convertToDTO(Message message, User sender, User receiver) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(sender != null ? sender.getUsername() : "");
        dto.setReceiverId(message.getReceiverId());
        dto.setReceiverName(receiver != null ? receiver.getUsername() : "");
        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setReadAt(message.getReadAt());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
