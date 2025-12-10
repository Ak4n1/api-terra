package com.ak4n1.terra.api.terra_api.notifications.exception;

/**
 * Excepción lanzada cuando hay un error de seguridad en WebSocket.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class WebSocketSecurityException extends RuntimeException {
    
    public WebSocketSecurityException(String message) {
        super(message);
    }
    
    public WebSocketSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}

