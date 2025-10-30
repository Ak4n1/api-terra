package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando un token (de verificación o reseteo) es inválido o ha expirado.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("TOKEN_EXPIRED");
    }
    
    public TokenExpiredException(String message) {
        super(message);
    }
}
