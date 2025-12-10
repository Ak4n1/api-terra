package com.ak4n1.terra.api.terra_api.notifications.exception;

/**
 * Excepción lanzada cuando el metadata de una notificación no cumple con el schema esperado.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class InvalidMetadataException extends RuntimeException {
    
    public InvalidMetadataException(String message) {
        super(message);
    }
    
    public InvalidMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}

