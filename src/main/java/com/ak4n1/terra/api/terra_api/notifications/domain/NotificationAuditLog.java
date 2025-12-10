package com.ak4n1.terra.api.terra_api.notifications.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * Entidad que registra la auditoría de todas las notificaciones creadas.
 * 
 * <p>Esta entidad permite rastrear quién creó cada notificación, cuándo,
 * y desde qué servicio/método. Es crítica para cumplimiento, debugging
 * y detección de abusos.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "notification_audit_log", indexes = {
    @Index(name = "idx_notification_id", columnList = "notification_id"),
    @Index(name = "idx_user_email", columnList = "user_email"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_service", columnList = "created_by_service")
})
public class NotificationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Notification is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @NotBlank(message = "Service name is required")
    @Column(name = "created_by_service", nullable = false, length = 100)
    private String createdByService; // ej: "PaymentServiceImpl"

    @NotBlank(message = "Method name is required")
    @Column(name = "created_by_method", nullable = false, length = 100)
    private String createdByMethod; // ej: "processPaymentSuccess"

    @NotBlank(message = "User email is required")
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "metadata_sanitized", columnDefinition = "JSON")
    private String metadataSanitized; // Versión sanitizada del metadata (sin datos sensibles)

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Constructor
    public NotificationAuditLog() {
        this.createdAt = new Date();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getCreatedByService() {
        return createdByService;
    }

    public void setCreatedByService(String createdByService) {
        this.createdByService = createdByService;
    }

    public String getCreatedByMethod() {
        return createdByMethod;
    }

    public void setCreatedByMethod(String createdByMethod) {
        this.createdByMethod = createdByMethod;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getMetadataSanitized() {
        return metadataSanitized;
    }

    public void setMetadataSanitized(String metadataSanitized) {
        this.metadataSanitized = metadataSanitized;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

