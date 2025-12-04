package com.ak4n1.terra.api.terra_api.withdrawal.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Entidad que representa un código de retiro de Terra Coins.
 * 
 * <p>Este código se genera cuando el usuario solicita retirar Terra Coins
 * desde la web al juego. El código debe ser ingresado en el juego (Alt+B)
 * para confirmar la operación.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "withdrawal_codes")
public class WithdrawalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(name = "code", length = 6, nullable = false)
    private String code;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private Timestamp usedAt;

    // Constructors
    public WithdrawalCode() {
    }

    public WithdrawalCode(String email, String code, Timestamp createdAt, Timestamp expiresAt) {
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Timestamp getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Timestamp usedAt) {
        this.usedAt = usedAt;
    }

    /**
     * Verifica si el código ha expirado.
     * 
     * @return true si el código ha expirado
     */
    public boolean isExpired() {
        return new Timestamp(System.currentTimeMillis()).after(expiresAt);
    }

    /**
     * Verifica si el código es válido (no usado y no expirado).
     * 
     * @return true si el código es válido
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
}

