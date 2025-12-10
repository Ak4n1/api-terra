package com.ak4n1.terra.api.terra_api.notifications.controllers;

import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationBroadcastRequest;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationCreateRequest;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationDTO;
import com.ak4n1.terra.api.terra_api.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de notificaciones.
 * 
 * <p>Proporciona endpoints para:
 * <ul>
 *   <li>Sincronización de notificaciones perdidas</li>
 *   <li>Exportación de datos (GDPR)</li>
 *   <li>Eliminación de datos (GDPR - Derecho al Olvido)</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Obtiene notificaciones no leídas del usuario autenticado.
     * Útil para sincronización cuando el usuario se reconecta.
     * 
     * @param authentication autenticación del usuario
     * @return lista de notificaciones no leídas
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Fetching unread notifications for user: {}", userEmail);
        
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Obtiene el conteo de notificaciones no leídas del usuario autenticado.
     * 
     * @param authentication autenticación del usuario
     * @return conteo de notificaciones no leídas
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = notificationService.getUnreadCount(userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Exporta todas las notificaciones del usuario en formato JSON (GDPR - Data Portability).
     * 
     * @param authentication autenticación del usuario
     * @return JSON con todas las notificaciones
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Exporting notifications for user: {} (GDPR - Data Portability)", userEmail);
        
        List<NotificationDTO> notifications = notificationService.exportUserNotifications(userEmail);
        
        // Convertir a JSON string
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notifications);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "notifications_" + userEmail + "_" + System.currentTimeMillis() + ".json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            logger.error("Error exporting notifications for user {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina todas las notificaciones del usuario autenticado (GDPR - Derecho al Olvido).
     * 
     * @param authentication autenticación del usuario
     * @return número de notificaciones eliminadas
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Deleting all notifications for user: {} (GDPR - Right to be forgotten)", userEmail);
        
        long deletedCount = notificationService.deleteAllUserNotifications(userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", "All notifications deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Marca una notificación como leída.
     * 
     * @param notificationId ID de la notificación
     * @param authentication autenticación del usuario
     * @return respuesta de éxito
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Long notificationId,
                                                           Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Marking notification {} as read for user: {}", notificationId, userEmail);
        
        notificationService.markAsRead(notificationId, userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * Marca todas las notificaciones del usuario como leídas.
     * 
     * @param authentication autenticación del usuario
     * @return respuesta de éxito
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Marking all notifications as read for user: {}", userEmail);
        
        notificationService.markAllAsRead(userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una notificación como administrador.
     * 
     * <p>Permite a los administradores crear notificaciones de cualquier tipo
     * para cualquier usuario. Útil para notificaciones administrativas, del sistema,
     * o comunicaciones masivas.
     * 
     * @param request los datos de la notificación
     * @param authentication autenticación del administrador
     * @return la notificación creada
     */
    @PostMapping("/admin/create")
    public ResponseEntity<Map<String, Object>> createNotificationAsAdmin(
            @Valid @RequestBody NotificationCreateRequest request,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        logger.info("Admin {} creating notification: type={}, user={}", 
                   adminEmail, request.getType(), request.getUserEmail());
        
        Notification notification = notificationService.createNotificationAsAdmin(request, adminEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification created successfully");
        response.put("notification", new NotificationDTO(notification));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crea una notificación broadcast para todos los usuarios activos.
     * 
     * <p>Permite a los administradores enviar notificaciones a todos los usuarios
     * activos del sistema (habilitados y con email verificado). Útil para
     * notificaciones del sistema, mantenimientos programados, anuncios generales, etc.
     * 
     * <p>La notificación se crea para cada usuario activo y se envía en tiempo real
     * vía WebSocket a los usuarios conectados. Los usuarios no conectados recibirán
     * la notificación cuando se sincronicen o recarguen la página.
     * 
     * @param request los datos de la notificación (sin userEmail)
     * @param authentication autenticación del administrador
     * @return resumen con el número de notificaciones creadas y enviadas
     */
    @PostMapping("/admin/create-broadcast")
    public ResponseEntity<Map<String, Object>> createBroadcastNotification(
            @Valid @RequestBody NotificationBroadcastRequest request,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        logger.info("Admin {} creating broadcast notification: type={}", 
                   adminEmail, request.getType());
        
        Map<String, Object> result = notificationService.createBroadcastNotification(request, adminEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}

