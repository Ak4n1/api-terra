package com.ak4n1.terra.api.terra_api.auth.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "account_deactivate_codes")
public class AccountDeactivateCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "deactivate_code", length = 255, nullable = false)
    private String deactivateCode;

    @Column(name = "deactivate_code_expire", nullable = false)
    private Timestamp deactivateCodeExpire;

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

    public String getDeactivateCode() {
        return deactivateCode;
    }

    public void setDeactivateCode(String deactivateCode) {
        this.deactivateCode = deactivateCode;
    }

    public Timestamp getDeactivateCodeExpire() {
        return deactivateCodeExpire;
    }

    public void setDeactivateCodeExpire(Timestamp deactivateCodeExpire) {
        this.deactivateCodeExpire = deactivateCodeExpire;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}

