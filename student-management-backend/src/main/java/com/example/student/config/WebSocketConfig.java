package com.example.student.config;

import com.example.student.websocket.NotificationWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration
 * 
 * Important: Spring's WebSocket handlers are registered at the servlet level,
 * NOT affected by server.servlet.context-path setting.
 * So if context-path is /api, the WebSocket endpoint will be at /api/ws/notification
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handlers");
        
        // The path here is relative to the servlet context path
        // With server.servlet.context-path=/api, actual URL will be: /api/ws/notification
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .setAllowedOrigins("*");
        
        // SockJS version as fallback
        registry.addHandler(notificationWebSocketHandler, "/ws/notification/sockjs")
                .setAllowedOrigins("*")
                .withSockJS();
        
        log.info("WebSocket handlers registered successfully");
        log.info("Access at: ws://localhost:8080/api/ws/notification?token=<JWT_TOKEN>");
    }
}
