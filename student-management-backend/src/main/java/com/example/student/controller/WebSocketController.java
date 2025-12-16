package com.example.student.controller;

import com.example.student.websocket.NotificationWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Controller for handling WebSocket connections
 */
@RestController
@RequestMapping("/ws")
@Slf4j
public class WebSocketController {
    
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    
    /**
     * Health check endpoint for WebSocket service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "websocket-notification");
        response.put("message", "WebSocket service is running. Connect to /api/ws/notification with token parameter.");
        return ResponseEntity.ok(response);
    }
}
