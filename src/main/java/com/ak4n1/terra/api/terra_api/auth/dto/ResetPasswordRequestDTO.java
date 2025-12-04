package com.ak4n1.terra.api.terra_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para petición de reset de contraseña usando token.
 * 
 * <p>Incluye validaciones para la nueva contraseña con requisitos de fortaleza.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ResetPasswordRequestDTO {

    /**
     * Nueva contraseña con requisitos de fortaleza
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one special character, and one number"
    )
    private String password;

    // Constructor por defecto
    public ResetPasswordRequestDTO() {
    }

    // Constructor con parámetros
    public ResetPasswordRequestDTO(String password) {
        this.password = password;
    }

    // Getters y Setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

