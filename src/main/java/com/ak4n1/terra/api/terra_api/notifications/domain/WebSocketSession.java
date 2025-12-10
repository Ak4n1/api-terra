package com.ak4n1.terra.api.terra_api.notifications.domain;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * Entidad que representa una sesión WebSocket activa.
 * 
 * <p>Esta entidad permite rastrear qué usuarios están conectados,
 * desde qué IP, y cuándo fue su última actividad. Es útil para:
 * - Encontrar sesiones activas de un usuario
 * - Limpiar sesiones expiradas
 * - Auditoría y seguridad
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "websocket_sessions", indexes = {
    @Index(name = "idx_user_email", columnList = "user_email"),
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_last_activity", columnList = "last_activity"),
    @Index(name = "idx_active_sessions", columnList = "user_email,disconnected_at")
})
public class WebSocketSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Session ID is required")
    @Column(name = "session_id", unique = true, nullable = false, length = 255)
    private String sessionId; // UUID único por conexión

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private AccountMaster user;

    @NotBlank(message = "IP address is required")
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "connected_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date connectedAt;

    @Column(name = "last_activity", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastActivity;

    @Column(name = "disconnected_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date disconnectedAt;

    // Constructor
    public WebSocketSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.connectedAt = new Date();
        this.lastActivity = new Date();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public AccountMaster getUser() {
        return user;
    }

    public void setUser(AccountMaster user) {
        this.user = user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Date connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Date getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(Date disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }

    // Métodos de utilidad
    /**
     * Verifica si la sesión está activa (no desconectada).
     * 
     * @return true si la sesión está activa, false en caso contrario
     */
    public boolean isActive() {
        return disconnectedAt == null;
    }

    /**
     * Marca la sesión como desconectada.
     */
    public void markAsDisconnected() {
        this.disconnectedAt = new Date();
    }

    /**
     * Actualiza la última actividad de la sesión.
     */
    public void updateLastActivity() {
        this.lastActivity = new Date();
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

