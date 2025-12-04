package com.ak4n1.terra.api.terra_api.streamer.controllers;

import com.ak4n1.terra.api.terra_api.security.services.CurrentUserResolver;
import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationDTO;
import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationResponseDTO;
import com.ak4n1.terra.api.terra_api.streamer.services.StreamerApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar solicitudes de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/streamer-applications")
public class StreamerApplicationController {

    private final StreamerApplicationService applicationService;
    private final CurrentUserResolver currentUserResolver;

    @Autowired
    public StreamerApplicationController(
            StreamerApplicationService applicationService,
            CurrentUserResolver currentUserResolver) {
        this.applicationService = applicationService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * Crea una nueva solicitud de streamer.
     * 
     * @param dto DTO con los datos de la solicitud
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la solicitud creada o error
     */
    @PostMapping
    public ResponseEntity<?> submitApplication(
            @Valid @RequestBody StreamerApplicationDTO dto,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        Long accountId = currentUserResolver.resolveCurrentUserId(authentication);
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Could not identify user"));
        }

        try {
            StreamerApplicationResponseDTO response = applicationService.submitApplication(dto, accountId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing application"));
        }
    }

    /**
     * Obtiene todas las solicitudes del usuario autenticado.
     * 
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la lista de solicitudes
     */
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        Long accountId = currentUserResolver.resolveCurrentUserId(authentication);
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Could not identify user"));
        }

        List<StreamerApplicationResponseDTO> applications = applicationService.getUserApplications(accountId);
        return ResponseEntity.ok(applications);
    }

    /**
     * Obtiene una solicitud específica por ID.
     * 
     * @param id ID de la solicitud
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la solicitud o error
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        Long accountId = currentUserResolver.resolveCurrentUserId(authentication);
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Could not identify user"));
        }

        try {
            StreamerApplicationResponseDTO application = applicationService.getApplicationById(id, accountId);
            return ResponseEntity.ok(application);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene todas las solicitudes aprobadas (solo para administradores).
     * 
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la lista de solicitudes aprobadas
     */
    @GetMapping("/admin/approved")
    public ResponseEntity<?> getApprovedApplications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        List<StreamerApplicationResponseDTO> applications = applicationService.getApprovedApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * Obtiene todas las solicitudes pendientes (solo para administradores).
     * 
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la lista de solicitudes pendientes
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingApplications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        List<StreamerApplicationResponseDTO> applications = applicationService.getPendingApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * Obtiene todas las solicitudes rechazadas (solo para administradores).
     * 
     * @param authentication contexto de autenticación
     * @return ResponseEntity con la lista de solicitudes rechazadas
     */
    @GetMapping("/admin/rejected")
    public ResponseEntity<?> getRejectedApplications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        List<StreamerApplicationResponseDTO> applications = applicationService.getRejectedApplications();
        return ResponseEntity.ok(applications);
    }
}

