package com.roadvision.store.config;

import com.roadvision.store.websocket.SubscriptionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Реєстрація WebSocket-ендпоінта /ws/{user_id}.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SubscriptionWebSocketHandler handler;

    public WebSocketConfig(SubscriptionWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/{userId}")
                .setAllowedOrigins("*");
    }
}
