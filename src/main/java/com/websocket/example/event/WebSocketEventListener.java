package com.websocket.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketEventListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Extract the unique Session ID from the connection headers
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            activeSessions.add(sessionId); // Adds the unique ID to our list
            log.info("New connection ({}). Total active users: {}", sessionId, activeSessions.size());
            broadcastViewerCount();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // The disconnect event provides the Session ID directly
        String sessionId = event.getSessionId();

        if (sessionId != null) {
            // Because this is a Set, removing the same ID twice does nothing (Idempotent!)
            activeSessions.remove(sessionId);
            log.info("Connection closed ({}). Total active users: {}", sessionId, activeSessions.size());
            broadcastViewerCount();
        }
    }

    public int getActiveUsers() {
        return activeSessions.size();
    }
    private void broadcastViewerCount() {
        // The viewer count is now just the true size of the active sessions list
        messagingTemplate.convertAndSend("/topic/viewers", activeSessions.size());
    }
}
