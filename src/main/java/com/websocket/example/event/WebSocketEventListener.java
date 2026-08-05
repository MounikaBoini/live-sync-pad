package com.websocket.example.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketEventListener {
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        messagingTemplate.convertAndSend("/topic/viewers", activeUsers.incrementAndGet());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        messagingTemplate.convertAndSend("/topic/viewers", activeUsers.decrementAndGet());
    }

    public int getActiveUsers() {
        return activeUsers.get();
    }
}
