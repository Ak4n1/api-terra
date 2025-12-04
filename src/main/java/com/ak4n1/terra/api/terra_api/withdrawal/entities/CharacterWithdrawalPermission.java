package com.ak4n1.terra.api.terra_api.withdrawal.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Entidad que representa el permiso de retiro de Terra Coins para un personaje.
 * 
 * <p>Solo los personajes con un registro en esta tabla pueden retirar
 * Terra Coins en el juego. Si no existe registro, el personaje no tiene
 * permiso y no podrá ver el balance ni realizar retiros.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "character_withdrawal_permissions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_email", "character_id"}))
public class CharacterWithdrawalPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_email", length = 100, nullable = false)
    private String accountEmail;

    @Column(name = "character_id", nullable = false)
    private Integer characterId;

    @Column(name = "character_name", length = 35, nullable = false)
    private String characterName;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    // Constructors
    public CharacterWithdrawalPermission() {
    }

    public CharacterWithdrawalPermission(String accountEmail, Integer characterId, String characterName) {
        this.accountEmail = accountEmail;
        this.characterId = characterId;
        this.characterName = characterName;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

