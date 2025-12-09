package com.ak4n1.terra.api.terra_api.payments.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el módulo de pagos
 * Previene exposición de stacktraces y maneja errores de forma consistente
 */
@RestControllerAdvice(basePackages = "com.ak4n1.terra.api.terra_api.payments")
public class PaymentExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentExceptionHandler.class);
    
    /**
     * Manejar violaciones de seguridad (price mismatch, invalid provider, etc.)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        logger.error("🚨 SECURITY VIOLATION in payment: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Payment blocked for security reasons");
        response.put("code", "SECURITY_VIOLATION");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Manejar argumentos inválidos (provider inválido, packageId negativo, etc.)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument in payment: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        response.put("code", "INVALID_ARGUMENT");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Manejar estado ilegal (saldo insuficiente, paquete inactivo, etc.)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        logger.warn("Illegal state in payment: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        response.put("code", "ILLEGAL_STATE");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Manejar errores generales de pago
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Unexpected error in payment system: {}", ex.getMessage(), ex);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "An error occurred processing your payment. Please try again later.");
        response.put("code", "INTERNAL_ERROR");
        
        // NO exponer el mensaje real de la excepción al cliente
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

