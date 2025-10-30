package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar un email que ya existe en el sistema.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS");
    }
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
