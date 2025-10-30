package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando se intenta autenticar con una cuenta deshabilitada.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("USER_DISABLED");
    }
    
    public UserDisabledException(String message) {
        super(message);
    }
}
