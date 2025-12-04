package com.ak4n1.terra.api.terra_api.withdrawal.controllers;

import com.ak4n1.terra.api.terra_api.withdrawal.dto.CharacterPermissionDTO;
import com.ak4n1.terra.api.terra_api.withdrawal.dto.PermissionRequestDTO;
import com.ak4n1.terra.api.terra_api.withdrawal.services.WithdrawalPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para gestionar permisos de retiro de Terra Coins por personaje.
 * 
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/withdrawal-permissions")
public class WithdrawalPermissionController {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalPermissionController.class);

    @Autowired
    private WithdrawalPermissionService permissionService;

    /**
     * Obtiene todos los personajes del usuario con su estado de permiso.
     * 
     * @return Lista de personajes con estado de permiso
     */
    @GetMapping
    public ResponseEntity<List<CharacterPermissionDTO>> getCharactersWithPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        logger.info("[WITHDRAWAL-PERMISSIONS] Get characters request from: {}", email);
        
        List<CharacterPermissionDTO> characters = permissionService.getCharactersWithPermissionStatus(email);
        
        return ResponseEntity.ok(characters);
    }

    /**
     * Otorga permiso de retiro a un personaje.
     * 
     * @param request Datos del personaje
     * @param httpRequest Request HTTP para obtener IP
     * @return Resultado de la operación
     */
    @PostMapping("/grant")
    public ResponseEntity<Map<String, Object>> grantPermission(
            @RequestBody PermissionRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String ipAddress = getClientIp(httpRequest);
        
        logger.info("[WITHDRAWAL-PERMISSIONS] Grant permission request from: {} for character: {}", 
                email, request.getCharacterId());
        
        boolean success = permissionService.grantPermission(
                email, 
                request.getCharacterId(), 
                request.getCharacterName(),
                ipAddress
        );
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Permission granted successfully"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to grant permission"
            ));
        }
    }

    /**
     * Revoca permiso de retiro a un personaje.
     * 
     * @param request Datos del personaje
     * @param httpRequest Request HTTP para obtener IP
     * @return Resultado de la operación
     */
    @PostMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revokePermission(
            @RequestBody PermissionRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String ipAddress = getClientIp(httpRequest);
        
        logger.info("[WITHDRAWAL-PERMISSIONS] Revoke permission request from: {} for character: {}", 
                email, request.getCharacterId());
        
        boolean success = permissionService.revokePermission(
                email, 
                request.getCharacterId(), 
                request.getCharacterName(),
                ipAddress
        );
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Permission revoked successfully"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to revoke permission"
            ));
        }
    }

    /**
     * Obtiene la IP real del cliente considerando proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

