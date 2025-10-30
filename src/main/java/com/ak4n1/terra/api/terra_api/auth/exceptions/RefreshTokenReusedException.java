package com.ak4n1.terra.api.terra_api.auth.exceptions;

/**
 * Excepción lanzada cuando se intenta reutilizar un refresh token que ya fue usado.
 * 
 * <p>Los refresh tokens solo pueden usarse una vez y deben ser rotados.
 * Si se intenta usar un refresh token que ya fue marcado como revocado,
 * se lanza esta excepción.
 * 
 * @see com.ak4n1.terra.api.terra_api.auth.entities.RefreshToken
 * @author ak4n1
 * @since 1.0
 */
public class RefreshTokenReusedException extends RuntimeException {
    
    /**
     * Constructor por defecto con mensaje estándar.
     */
    public RefreshTokenReusedException() {
        super("REFRESH_TOKEN_REUSED");
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje de error personalizado
     */
    public RefreshTokenReusedException(String message) {
        super(message);
    }
}

