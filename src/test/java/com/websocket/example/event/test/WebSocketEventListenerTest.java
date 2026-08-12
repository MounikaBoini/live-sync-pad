package com.websocket.example.event.test;

import com.websocket.example.event.WebSocketEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    private final String testSessionId = "session-12345";
    private final String testUsername = "Mounika";
    SimpMessageHeaderAccessor accessor;

    @BeforeEach
    void setup(){
        accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionId(testSessionId);
    }
    @Test
    void testHandleWebSocketConnectListener() {
        accessor.setNativeHeader("username", testUsername);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionConnectEvent connectEvent = new SessionConnectEvent(this, message, null);
        webSocketEventListener.handleWebSocketConnectListener(connectEvent);

        assertEquals(1, webSocketEventListener.getActiveUsers());
        verify(messagingTemplate, times(1)).convertAndSend("/topic/viewers", 1);
    }

    @Test
    void testHandleWebSocketDisconnectListener() {
        accessor.setNativeHeader("username", testUsername);
        Message<byte[]> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        webSocketEventListener.handleWebSocketConnectListener(new SessionConnectEvent(this, connectMessage, null));
        reset(messagingTemplate);
        SimpMessageHeaderAccessor disconnectAccessor =  SimpMessageHeaderAccessor.create();
        disconnectAccessor.setSessionId(testSessionId);
        Message<byte[]> disconnectMessage = MessageBuilder.createMessage(new byte[0], disconnectAccessor.getMessageHeaders());
        SessionDisconnectEvent disconnectEvent = new SessionDisconnectEvent(this, disconnectMessage, testSessionId, null);
        webSocketEventListener.handleWebSocketDisconnectListener(disconnectEvent);

        assertEquals(0, webSocketEventListener.getActiveUsers());

        verify(messagingTemplate, times(1)).convertAndSend("/topic/leave", testUsername);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/viewers", 0);
    }

    @Test
    void testHandleWebSocketConnectListener_WithoutUsername() {
        accessor.setSessionId("session-999");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionConnectEvent connectEvent = new SessionConnectEvent(this, message, null);
        webSocketEventListener.handleWebSocketConnectListener(connectEvent);

        assertEquals(0, webSocketEventListener.getActiveUsers());
        verify(messagingTemplate, times(1)).convertAndSend("/topic/viewers", 0);
    }
}