package com.ak4n1.terra.api.terra_api.notifications.dto;

import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.Map;

/**
 * DTO para crear una notificación broadcast (para todos los usuarios activos).
 * 
 * <p>Este DTO es usado por administradores para enviar notificaciones
 * a todos los usuarios activos del sistema (habilitados y con email verificado).
 * 
 * @author ak4n1
 * @since 1.0
 */
public class NotificationBroadcastRequest {

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Map<String, Object> metadata; // Se convertirá a JSON string

    private Date expiresAt; // Opcional

    // Constructor por defecto
    public NotificationBroadcastRequest() {}

    // Constructor completo
    public NotificationBroadcastRequest(NotificationType type, String title,
                                      String message, Map<String, Object> metadata, Date expiresAt) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
        this.expiresAt = expiresAt;
    }

    // Getters y Setters
    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
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

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}

