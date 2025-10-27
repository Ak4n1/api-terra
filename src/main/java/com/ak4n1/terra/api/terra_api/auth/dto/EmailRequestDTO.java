package com.ak4n1.terra.api.terra_api.auth.dto;

// DTO para recibir el email desde el body
public class EmailRequestDTO {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
