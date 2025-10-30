package com.ak4n1.terra.api.terra_api.auth.dto;

/**
 * DTO para recibir un email desde el body de una petición.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class EmailRequestDTO {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
