package com.ak4n1.terra.api.terra_api.notifications.exception;

/**
 * Excepción lanzada cuando un servicio no autorizado intenta crear una notificación.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class UnauthorizedNotificationException extends RuntimeException {
    
    public UnauthorizedNotificationException(String message) {
        super(message);
    }
    
    public UnauthorizedNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

