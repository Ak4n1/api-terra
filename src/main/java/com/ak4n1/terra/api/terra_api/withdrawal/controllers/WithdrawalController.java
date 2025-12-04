package com.ak4n1.terra.api.terra_api.withdrawal.controllers;

import com.ak4n1.terra.api.terra_api.withdrawal.services.WithdrawalCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para gestionar códigos de retiro de Terra Coins.
 * 
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/withdrawal")
public class WithdrawalController {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalController.class);

    @Autowired
    private WithdrawalCodeService withdrawalCodeService;

    /**
     * Genera un código de retiro y lo envía por email.
     * 
     * @return Respuesta con el resultado de la operación
     */
    @PostMapping("/generate-code")
    public ResponseEntity<Map<String, Object>> generateCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        logger.info("[WITHDRAWAL] Generate code request from: {}", email);
        
        Map<String, Object> response = withdrawalCodeService.generateAndSendCode(email);
        
        String status = (String) response.get("status");
        String errorCode = (String) response.get("errorCode");
        
        if ("error".equals(status)) {
            // Determinar el código HTTP según el tipo de error
            if ("COOLDOWN".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            } else if ("ZERO_BALANCE".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else if ("USER_NOT_FOUND".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Valida un código de retiro (endpoint para L2J).
     * Este endpoint puede ser llamado internamente o desde el servidor de juego.
     * 
     * @param email Email del usuario
     * @param code Código de 6 dígitos
     * @return true si el código es válido
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCode(
            @RequestParam String email,
            @RequestParam String code) {
        
        logger.info("[WITHDRAWAL] Validate code request for: {}", email);
        
        boolean isValid = withdrawalCodeService.validateCode(email, code);
        
        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "email", email
        ));
    }

    /**
     * Marca un código como usado (endpoint para L2J).
     * 
     * @param email Email del usuario
     * @param code Código de 6 dígitos
     * @return Resultado de la operación
     */
    @PostMapping("/mark-used")
    public ResponseEntity<Map<String, Object>> markCodeAsUsed(
            @RequestParam String email,
            @RequestParam String code) {
        
        logger.info("[WITHDRAWAL] Mark code as used request for: {}", email);
        
        boolean success = withdrawalCodeService.markCodeAsUsed(email, code);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Code marked as used"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", "Invalid or expired code"
            ));
        }
    }
}

