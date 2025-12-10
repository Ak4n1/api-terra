package com.ak4n1.terra.api.terra_api.notifications.dto;

import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO para mensajes WebSocket.
 * 
 * <p>Representa los mensajes que se envían y reciben a través de WebSocket.
 * El campo type determina el tipo de mensaje y qué otros campos están presentes.
 * 
 * @author ak4n1
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {

    /**
     * Tipo de mensaje WebSocket.
     */
    public enum MessageType {
        // Mensajes del servidor al cliente
        NOTIFICATION,           // Nueva notificación
        PING,                   // Heartbeat ping
        TOKEN_REFRESH_REQUIRED, // Token próximo a expirar
        IDLE_TIMEOUT,           // Advertencia de timeout inactivo
        SERVER_SHUTDOWN,        // Servidor se está cerrando
        
        // Mensajes del cliente al servidor
        PONG,                   // Respuesta a ping
        ACK,                    // Acknowledgment de notificación recibida
        MARK_AS_READ           // Marcar notificación como leída
    }

    private MessageType type;
    private Long notificationId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    private Long timestamp;
    private Integer reconnectInSeconds;

    // Constructor por defecto
    public WebSocketMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor para mensajes de notificación
    public WebSocketMessage(MessageType type, Long notificationId, NotificationType notificationType,
                           String title, String message, Map<String, Object> metadata) {
        this.type = type;
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor para mensajes simples
    public WebSocketMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getReconnectInSeconds() {
        return reconnectInSeconds;
    }

    public void setReconnectInSeconds(Integer reconnectInSeconds) {
        this.reconnectInSeconds = reconnectInSeconds;
    }
}

