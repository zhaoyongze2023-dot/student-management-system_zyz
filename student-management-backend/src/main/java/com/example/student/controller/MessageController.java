package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.MessageDTO;
import com.example.student.dto.PageResponse;
import com.example.student.entity.User;
import com.example.student.repository.UserRepository;
import com.example.student.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;
    
    /**
     * 从 SecurityContext 中获取当前登录用户的 ID
     * 返回 null 如果未登录
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("用户未认证或认证为 null");
                return null;
            }
            
            String username = authentication.getName();
            log.debug("从 Token 中提取的用户名: {}", username);
            
            if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
                log.warn("用户名为空或为匿名用户: {}", username);
                return null;
            }
            
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                log.error("数据库中找不到用户: {}", username);
                return null;
            }
            
            Long userId = user.get().getId();
            log.debug("获取到用户 ID: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("获取当前用户 ID 时发生异常", e);
            return null;
        }
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        try {
            MessageDTO messageDTO = messageService.sendMessage(senderId, receiverId, content);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("消息发送成功", messageDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "消息发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户消息列表（前端调用 /notification/messages）
     * 不需要前端传 ID，直接从 JWT Token 中获取
     */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<MessageDTO> page = messageService.getInboxMessages(receiverId, pageable);
            
            PageResponse<MessageDTO> response = new PageResponse<MessageDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            log.error("获取消息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收件箱
     */
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(
            @RequestParam Long receiverId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<MessageDTO> page = messageService.getInboxMessages(receiverId, pageable);
            
            PageResponse<MessageDTO> response = new PageResponse<MessageDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取收件箱失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息
     * 不需要前端传 ID，直接从 JWT Token 中获取
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<MessageDTO> page = messageService.getUnreadMessages(receiverId, pageable);
            
            PageResponse<MessageDTO> response = new PageResponse<MessageDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            log.error("获取未读消息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取未读消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取消息会话
     */
    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(current - 1, size);
            Page<MessageDTO> page = messageService.getConversation(userId1, userId2, pageable);
            
            PageResponse<MessageDTO> response = new PageResponse<MessageDTO>(
                    page.getTotalElements(),
                    current,
                    size,
                    page.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取消息会话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记消息为已读（内部调用方法）
     * 不需要前端传 ID，直接从 JWT Token 中获取用户 ID 进行验证
     */
    private ResponseEntity<?> performMarkAsRead(Long id) {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            messageService.markAsRead(id, receiverId);
            return ResponseEntity.ok(ApiResponse.success("消息已标记为已读", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 标记消息为已读
     * 支持 /notification/{id}/read 路由
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return performMarkAsRead(id);
    }

    /**
     * 标记消息为已读（兼容路由）
     * 支持 /notification/messages/{id}/read 路由（前端可能使用的路由）
     */
    @PostMapping("/messages/{id}/read")
    public ResponseEntity<?> markAsReadViaMessages(@PathVariable Long id) {
        return performMarkAsRead(id);
    }
    
    /**
     * 标记所有消息为已读
     * 不需要前端传 ID，直接从 JWT Token 中获取
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead() {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            messageService.markMultipleAsRead(receiverId);
            return ResponseEntity.ok(ApiResponse.success("所有消息已标记为已读", null));
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息数量
     * 不需要前端传 ID，直接从 JWT Token 中获取
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            Integer count = messageService.getUnreadCount(receiverId);
            Map<String, Object> data = new HashMap<>();
            data.put("unreadCount", count);
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取未读消息数量失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.ok(ApiResponse.success("消息删除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "消息删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最新消息
     * 不需要前端传 ID，直接从 JWT Token 中获取
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestMessages(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // 从 SecurityContext 获取当前用户 ID（来自 JWT Token）
            Long receiverId = getCurrentUserId();
            if (receiverId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权，请先登录"));
            }
            
            List<MessageDTO> messages = messageService.getLatestMessages(receiverId, limit);
            return ResponseEntity.ok(ApiResponse.success("获取成功", messages));
        } catch (Exception e) {
            log.error("获取最新消息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取最新消息失败: " + e.getMessage()));
        }
    }
}
