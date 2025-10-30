package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un usuario con el criterio de búsqueda dado.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
}
