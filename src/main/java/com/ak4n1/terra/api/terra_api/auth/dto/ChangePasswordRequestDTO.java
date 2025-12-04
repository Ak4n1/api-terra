package com.ak4n1.terra.api.terra_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para petición de cambio de contraseña de usuario.
 * 
 * <p>Incluye validaciones para la contraseña actual (opcional para usuarios OAuth)
 * y la nueva contraseña con requisitos de fortaleza.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ChangePasswordRequestDTO {

    /**
     * Contraseña actual (opcional para usuarios OAuth sin password)
     */
    private String currentPassword;

    /**
     * Nueva contraseña con requisitos de fortaleza
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*\\d).*$",
        message = "New password must contain at least one uppercase letter, one special character, and one number"
    )
    private String newPassword;

    // Constructor por defecto
    public ChangePasswordRequestDTO() {
    }

    // Constructor con parámetros
    public ChangePasswordRequestDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    // Getters y Setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

