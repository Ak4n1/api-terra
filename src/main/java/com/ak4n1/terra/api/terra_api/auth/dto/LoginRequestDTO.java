package com.ak4n1.terra.api.terra_api.auth.dto;


/**
 * DTO para petición de login con email y contraseña.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class LoginRequestDTO {
    private String email;
    private String password;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
