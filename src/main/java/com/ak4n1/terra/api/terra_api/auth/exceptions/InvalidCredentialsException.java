package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando las credenciales de autenticación son inválidas.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
