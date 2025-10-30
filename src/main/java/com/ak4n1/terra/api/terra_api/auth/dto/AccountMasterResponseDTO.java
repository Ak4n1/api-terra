package com.ak4n1.terra.api.terra_api.auth.dto;


import java.util.Date;
import java.util.List;

/**
 * DTO para respuesta con información de un usuario.
 * 
 * <p>Contiene los datos públicos del usuario para enviar en respuestas JSON.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class AccountMasterResponseDTO {
    private Long id;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private List<String> roles;
    private Integer terraCoins;
    private Date createdAt;
    private boolean twoFactorEnabled;
    private boolean hasPassword;  // indica si el usuario tiene password (false si se creó con Google)


    public AccountMasterResponseDTO() {
    }

    public AccountMasterResponseDTO(Long id, String email, boolean enabled, boolean emailVerified, List<String> roles, Integer terraCoins) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.roles = roles;
        this.terraCoins = terraCoins;
    }

    // getters y setters
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public Integer getTerraCoins() {
        return terraCoins;
    }

    public void setTerraCoins(Integer terraCoins) {
        this.terraCoins = terraCoins;
    }
    private String googleUid;

    public String getGoogleUid() {
        return googleUid;
    }

    public void setGoogleUid(String googleUid) {
        this.googleUid = googleUid;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }
}
