package com.ak4n1.terra.api.terra_api.auth.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Entidad que representa un registro de desactivación de cuenta.
 * 
 * <p>Registra cuándo y por qué motivo se desactivó una cuenta,
 * ya sea por decisión del usuario o por acción de un administrador.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "account_deactivations")
public class AccountDeactivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_master_id", nullable = false)
    private AccountMaster accountMaster;

    @Column(name = "deactivation_date", nullable = false)
    private Timestamp deactivationDate;

    @Column(name = "reason", nullable = false, length = 50)
    private String reason; // "USER" o "ADMIN"

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = true)
    private AccountMaster admin; // Solo si reason = "ADMIN"

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Comentarios opcionales del admin

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountMaster getAccountMaster() {
        return accountMaster;
    }

    public void setAccountMaster(AccountMaster accountMaster) {
        this.accountMaster = accountMaster;
    }

    public Timestamp getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(Timestamp deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AccountMaster getAdmin() {
        return admin;
    }

    public void setAdmin(AccountMaster admin) {
        this.admin = admin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

