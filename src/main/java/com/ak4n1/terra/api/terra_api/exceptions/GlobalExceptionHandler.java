package com.ak4n1.terra.api.terra_api.exceptions;

import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailAlreadyExistsException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.InvalidCredentialsException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.RefreshTokenReusedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.TokenExpiredException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserDisabledException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException;
import com.ak4n1.terra.api.terra_api.game.exceptions.AccountAlreadyExistsException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeAlreadyUsedException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeExpiredException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeNotFoundException;
import com.ak4n1.terra.api.terra_api.game.exceptions.GameAccountNotFoundException;
import com.ak4n1.terra.api.terra_api.game.exceptions.InvalidCreationCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja la excepción cuando un email ya está registrado
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        logger.warn("❌ [EXCEPTION] Email ya existe: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "EMAIL_ALREADY_EXISTS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja la excepción cuando un usuario no se encuentra
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        logger.warn("❌ [EXCEPTION] Usuario no encontrado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "USER_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja la excepción cuando las credenciales son inválidas
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        logger.warn("❌ [EXCEPTION] Credenciales inválidas: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja la excepción cuando un token expiró o es inválido
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        logger.warn("❌ [EXCEPTION] Token expirado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "TOKEN_EXPIRED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja la excepción cuando el email no está verificado
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        logger.warn("❌ [EXCEPTION] Email no verificado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "EMAIL_NOT_VERIFIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja la excepción cuando un usuario está deshabilitado
     */
    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<Map<String, Object>> handleUserDisabled(UserDisabledException ex) {
        logger.warn("❌ [EXCEPTION] Usuario deshabilitado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "USER_DISABLED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja la excepción cuando se intenta reutilizar un refresh token
     */
    @ExceptionHandler(RefreshTokenReusedException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenReused(RefreshTokenReusedException ex) {
        logger.warn("❌ [EXCEPTION] Refresh token reusado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Refresh token has already been used and cannot be reused");
        error.put("error", "REFRESH_TOKEN_REUSED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ============ GAME EXCEPTIONS ============

    /**
     * Maneja la excepción cuando una cuenta de juego ya existe
     */
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAccountAlreadyExists(AccountAlreadyExistsException ex) {
        logger.warn("❌ [GAME EXCEPTION] Cuenta de juego ya existe: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "GAME_ACCOUNT_ALREADY_EXISTS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja la excepción cuando el código de creación no se encuentra
     */
    @ExceptionHandler(CreationCodeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCreationCodeNotFound(CreationCodeNotFoundException ex) {
        logger.warn("❌ [GAME EXCEPTION] Código de creación no encontrado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "CREATION_CODE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja la excepción cuando el código de creación ya fue usado
     */
    @ExceptionHandler(CreationCodeAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleCreationCodeAlreadyUsed(CreationCodeAlreadyUsedException ex) {
        logger.warn("❌ [GAME EXCEPTION] Código de creación ya usado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "CREATION_CODE_ALREADY_USED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja la excepción cuando el código de creación es inválido
     */
    @ExceptionHandler(InvalidCreationCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCreationCode(InvalidCreationCodeException ex) {
        logger.warn("❌ [GAME EXCEPTION] Código de creación inválido: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "INVALID_CREATION_CODE");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja la excepción cuando el código de creación expiró
     */
    @ExceptionHandler(CreationCodeExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleCreationCodeExpired(CreationCodeExpiredException ex) {
        logger.warn("❌ [GAME EXCEPTION] Código de creación expirado: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "CREATION_CODE_EXPIRED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja la excepción cuando una cuenta de juego no se encuentra
     */
    @ExceptionHandler(GameAccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameAccountNotFound(GameAccountNotFoundException ex) {
        logger.warn("❌ [GAME EXCEPTION] Cuenta de juego no encontrada: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error", "GAME_ACCOUNT_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

