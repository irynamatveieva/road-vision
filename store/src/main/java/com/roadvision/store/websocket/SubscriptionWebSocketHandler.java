package com.roadvision.store.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хендлер WebSocket-підписок. UI-клієнт підключається до /ws/{user_id} і
 * отримує оновлення даних саме для свого user_id (аналог subscriptions у методичці).
 */
@Component
public class SubscriptionWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // user_id -> множина активних WebSocket-сесій
    private final Map<Integer, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();

    public SubscriptionWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private int extractUserId(WebSocketSession session) {
        String path = session.getUri().getPath();      // напр. /ws/1
        String last = path.substring(path.lastIndexOf('/') + 1);
        return Integer.parseInt(last);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        int userId = extractUserId(session);
        subscriptions
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        int userId = extractUserId(session);
        Set<WebSocketSession> sessions = subscriptions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    /** Розсилає дані всім підписникам відповідного user_id. */
    public void sendDataToSubscribers(int userId, Object data) {
        Set<WebSocketSession> sessions = subscriptions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(data));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException e) {
            // ігноруємо помилки окремих сесій, щоб не зривати основний потік
        }
    }
}
