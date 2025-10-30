package com.ak4n1.terra.api.terra_api.auth.services;

import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interfaz del servicio de autenticación.
 * 
 * <p>Define las operaciones para registro, verificación de email,
 * recuperación de contraseña y gestión de usuarios.
 * 
 * @see AuthServiceImpl
 * @author ak4n1
 * @since 1.0
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param registerRequest DTO con email y contraseña del nuevo usuario
     * @return ResponseEntity con el resultado del registro
     */
    ResponseEntity<?> save(RegisterRequestDTO registerRequest);

    /**
     * Envía un email de recuperación de contraseña al usuario.
     * 
     * @param email Email del usuario que solicita el reseteo
     * @return Map con el resultado (success, message, minutesLeft si aplica)
     */
    Map<String, Object> sendPasswordResetEmail(String email);

    /**
     * Restablece la contraseña del usuario usando un token de reseteo.
     * 
     * @param tokenUser Token de reseteo de contraseña
     * @param newPassword Nueva contraseña del usuario
     * @return Map con el resultado (success, message)
     */
    Map<String, Object> resetPassword(String tokenUser, String newPassword);

    /**
     * Reenvía el email de verificación al usuario.
     * 
     * @param email Email del usuario
     * @return Map con el resultado (status: success/error/forbidden, message)
     */
    Map<String, String> resendVerificationEmail(String email);

    /**
     * Verifica el email del usuario usando el token de verificación.
     * 
     * @param token Token de verificación recibido por email
     * @return ResponseEntity con el resultado de la verificación
     */
    ResponseEntity<?> verifyEmail(String token);
    
    /**
     * Obtiene la información del usuario actual por su email.
     * 
     * @param email Email del usuario
     * @return Map con los datos del usuario (id, email, enabled, emailVerified, roles, terraCoins, etc.)
     */
    Map<String, Object> getCurrentUser(String email);

}
