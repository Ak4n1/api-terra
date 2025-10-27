package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_create_codes")
public class AccountCreateCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(name = "create_code", length = 255, nullable = false)
    private String createCode;

    @Column(name = "create_code_expire", nullable = false)
    private Timestamp createCodeExpire;

    @Column(name = "used", nullable = false)
    private boolean used = false;
    // Getters y setters

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

    public String getCreateCode() {
        return createCode;
    }

    public void setCreateCode(String createCode) {
        this.createCode = createCode;
    }

    public Timestamp getCreateCodeExpire() {
        return createCodeExpire;
    }

    public void setCreateCodeExpire(Timestamp createCodeExpire) {
        this.createCodeExpire = createCodeExpire;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
