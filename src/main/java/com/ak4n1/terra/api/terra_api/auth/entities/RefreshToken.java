package com.ak4n1.terra.api.terra_api.auth.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa un refresh token JWT activo en el sistema.
 * 
 * <p>Almacena refresh tokens JWT válidos asociados a usuarios para permitir
 * la renovación de access tokens. Incluye información de expiración, revocación
 * y tipo de dispositivo para control de sesiones.
 * 
 * @see AccountMaster
 * @see ActiveToken
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512, nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountMaster accountMaster;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "expires_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * Callback JPA que inicializa createdAt antes de persistir.
     * La expiración debe ser establecida explícitamente al crear el token.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AccountMaster getAccountMaster() {
        return accountMaster;
    }

    public void setAccountMaster(AccountMaster accountMaster) {
        this.accountMaster = accountMaster;
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

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}

