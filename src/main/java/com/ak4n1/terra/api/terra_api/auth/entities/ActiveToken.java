package com.ak4n1.terra.api.terra_api.auth.entities; // corregí "entitites" a "entities"

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa un token JWT activo en el sistema.
 * 
 * <p>Almacena tokens JWT válidos asociados a usuarios para control de sesiones.
 * Incluye información de expiración y tipo de dispositivo.
 * 
 * @see AccountMaster
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "active_tokens")
public class ActiveToken {

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

    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * Callback JPA que inicializa createdAt y expiresAt antes de persistir.
     * Si expiresAt no está configurado, establece 1 hora por defecto.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.expiresAt = new Date(System.currentTimeMillis() + 3600000); // 1 hora en ms
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
