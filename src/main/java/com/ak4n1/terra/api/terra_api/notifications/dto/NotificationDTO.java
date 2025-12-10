package com.ak4n1.terra.api.terra_api.notifications.dto;

import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * DTO para transferencia de notificaciones.
 * 
 * @author ak4n1
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String metadata; // JSON string
    private Boolean read;
    private Date readAt;
    private Date createdAt;
    private Date expiresAt;

    // Constructor por defecto
    public NotificationDTO() {}

    // Constructor desde entidad
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.metadata = notification.getMetadata();
        this.read = notification.isRead();
        this.readAt = notification.getReadAt();
        this.createdAt = notification.getCreatedAt();
        this.expiresAt = notification.getExpiresAt();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Date getReadAt() {
        return readAt;
    }

    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}

