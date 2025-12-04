package com.ak4n1.terra.api.terra_api.withdrawal.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Entidad para auditoría de cambios en permisos de retiro.
 * 
 * <p>Registra cada vez que se otorga o revoca un permiso de retiro
 * de Terra Coins a un personaje, incluyendo la IP desde donde se hizo.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "withdrawal_permissions_audit")
public class WithdrawalPermissionAudit {

    public enum Action {
        GRANTED,
        REVOKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_email", length = 100, nullable = false)
    private String accountEmail;

    @Column(name = "character_id", nullable = false)
    private Integer characterId;

    @Column(name = "character_name", length = 35, nullable = false)
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 10, nullable = false)
    private Action action;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    // Constructors
    public WithdrawalPermissionAudit() {
    }

    public WithdrawalPermissionAudit(String accountEmail, Integer characterId, String characterName, 
                                      Action action, String ipAddress) {
        this.accountEmail = accountEmail;
        this.characterId = characterId;
        this.characterName = characterName;
        this.action = action;
        this.ipAddress = ipAddress;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public Integer getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

