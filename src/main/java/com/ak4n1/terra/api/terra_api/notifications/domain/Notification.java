package com.ak4n1.terra.api.terra_api.notifications.domain;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * Entidad que representa una notificación del sistema.
 * 
 * <p>Las notificaciones se almacenan en la base de datos y se distribuyen
 * en tiempo real a través de WebSocket a los usuarios conectados.
 * 
 * <p>El campo metadata es un JSON flexible que permite extender notificaciones
 * sin modificar el esquema de base de datos.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_email", columnList = "user_email"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_read_at", columnList = "read_at"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_unread", columnList = "user_email,read_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User email is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private AccountMaster user;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // JSON string para flexibilidad

    @Column(name = "read_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    // Constructor
    public Notification() {
        this.createdAt = new Date();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountMaster getUser() {
        return user;
    }

    public void setUser(AccountMaster user) {
        this.user = user;
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

    // Métodos de utilidad
    /**
     * Verifica si la notificación ha sido leída.
     * 
     * @return true si la notificación ha sido leída, false en caso contrario
     */
    public boolean isRead() {
        return readAt != null;
    }

    /**
     * Marca la notificación como leída.
     */
    public void markAsRead() {
        this.readAt = new Date();
    }

    /**
     * Verifica si la notificación ha expirado.
     * 
     * @return true si la notificación ha expirado, false en caso contrario
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return new Date().after(expiresAt);
    }

    /**
     * Obtiene el email del usuario (método de conveniencia).
     * 
     * @return el email del usuario, o null si el usuario no está cargado
     */
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }
}

