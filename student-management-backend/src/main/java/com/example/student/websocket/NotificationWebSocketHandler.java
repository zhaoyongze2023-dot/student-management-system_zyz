package com.example.student.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket Handler for real-time notifications
 */
@Component
@Slf4j
public class NotificationWebSocketHandler implements WebSocketHandler {
    
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket connection established. Total connections: {}", sessions.size());
        
        // Send welcome message
        session.sendMessage(new org.springframework.web.socket.TextMessage(
                "{\"type\":\"connection\",\"message\":\"Connected to notification service\",\"timestamp\":" + System.currentTimeMillis() + "}"
        ));
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("Received message from {}: {}", session.getId(), message.getPayload());
        
        // Echo message back or handle custom logic here
        if (message instanceof org.springframework.web.socket.TextMessage) {
            String payload = ((org.springframework.web.socket.TextMessage) message).getPayload();
            session.sendMessage(new org.springframework.web.socket.TextMessage(
                    "{\"type\":\"echo\",\"data\":" + payload + ",\"timestamp\":" + System.currentTimeMillis() + "}"
            ));
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        log.info("WebSocket connection closed. Reason: {}. Total connections: {}", closeStatus.getReason(), sessions.size());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Broadcast message to all connected clients
     */
    public static void broadcastMessage(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(
                        "{\"type\":\"notification\",\"data\":" + message + ",\"timestamp\":" + System.currentTimeMillis() + "}"
                ));
            }
        }
    }
    
    /**
     * Send message to specific client
     */
    public static void sendMessageToSession(String sessionId, String message) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.getId().equals(sessionId) && session.isOpen()) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(
                        "{\"type\":\"notification\",\"data\":" + message + ",\"timestamp\":" + System.currentTimeMillis() + "}"
                ));
                break;
            }
        }
    }
}
