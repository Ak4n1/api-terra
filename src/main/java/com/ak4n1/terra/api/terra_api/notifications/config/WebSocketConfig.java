package com.ak4n1.terra.api.terra_api.notifications.config;

import com.ak4n1.terra.api.terra_api.notifications.websocket.NotificationWebSocketHandler;
import com.ak4n1.terra.api.terra_api.notifications.websocket.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración de WebSocket para el sistema de notificaciones.
 * 
 * <p>Configura el endpoint WebSocket en /api/notifications/ws y registra
 * el handler y el interceptor de handshake.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler webSocketHandler;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    public WebSocketConfig(NotificationWebSocketHandler webSocketHandler,
                          WebSocketHandshakeInterceptor handshakeInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/api/notifications/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(
                        "https://l2terra.online",
                        "http://localhost:4200",
                        "file://"
                );
    }
}

