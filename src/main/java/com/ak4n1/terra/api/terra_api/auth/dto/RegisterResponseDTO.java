package com.ak4n1.terra.api.terra_api.auth.dto;

/**
 * DTO para respuesta de registro de usuario.
 * 
 * <p>Contiene el status HTTP y un mensaje descriptivo del resultado.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class RegisterResponseDTO {

    private int status;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}


