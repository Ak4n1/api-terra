package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando se intenta autenticar con un email que no está verificado.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("EMAIL_NOT_VERIFIED");
    }
    
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
