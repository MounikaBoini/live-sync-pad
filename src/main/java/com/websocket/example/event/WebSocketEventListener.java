package com.websocket.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketEventListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String,String> sessionUserMap = new ConcurrentHashMap<>(); // map to store username - their sessionID

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) { // Note: ConnectEvent!
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            List<String> usernameHeader = accessor.getNativeHeader("username");
            if (usernameHeader != null && !usernameHeader.isEmpty()) {
                String username = usernameHeader.get(0);
                sessionUserMap.put(sessionId, username);
                log.info("User {} joined. Total active users: {}", username, sessionUserMap.size());
            }
            broadcastViewerCount();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // The disconnect event provides the Session ID directly
        String sessionId = event.getSessionId();

        if (sessionId != null) {
            String disconnectedUser = sessionUserMap.remove(sessionId);
            if (disconnectedUser != null) {
                log.info("User {} disconnected. Total active users: {}", disconnectedUser, sessionUserMap.size());
                messagingTemplate.convertAndSend("/topic/leave", disconnectedUser);
            }
            broadcastViewerCount();
        }
    }

    public int getActiveUsers() {
        return sessionUserMap.size();
    }
    private void broadcastViewerCount() {
        // The viewer count is now just the true size of the active sessions list
        messagingTemplate.convertAndSend("/topic/viewers", getActiveUsers());
    }
}
