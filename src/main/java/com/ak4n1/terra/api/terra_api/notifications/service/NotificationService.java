package com.ak4n1.terra.api.terra_api.notifications.service;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationAuditLog;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationBroadcastRequest;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationCreateRequest;
import com.ak4n1.terra.api.terra_api.notifications.dto.NotificationDTO;
import com.ak4n1.terra.api.terra_api.notifications.exception.InvalidMetadataException;
import com.ak4n1.terra.api.terra_api.notifications.exception.UnauthorizedNotificationException;
import com.ak4n1.terra.api.terra_api.notifications.repository.NotificationAuditRepository;
import com.ak4n1.terra.api.terra_api.notifications.repository.NotificationRepository;
import com.ak4n1.terra.api.terra_api.notifications.websocket.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio principal para gestión de notificaciones.
 * 
 * <p>Implementa gobernanza estricta: solo servicios que implementan
 * NotificationSender pueden crear notificaciones. Incluye sanitización
 * de metadata, auditoría completa y distribución en tiempo real vía WebSocket.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationAuditRepository auditRepository;
    private final AccountMasterRepository accountMasterRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    // Configuración de retención (en días)
    private static final int RETENTION_UNREAD_DAYS = 30;
    private static final int RETENTION_READ_DAYS = 7;
    private static final int RETENTION_ARCHIVED_DAYS = 90;

    // Frecuencia máxima de notificaciones
    private static final int MAX_NOTIFICATIONS_PER_USER_PER_MINUTE = 10;
    private static final int MAX_NOTIFICATIONS_SAME_TYPE_PER_HOUR = 3;

    public NotificationService(NotificationRepository notificationRepository,
                              NotificationAuditRepository auditRepository,
                              AccountMasterRepository accountMasterRepository,
                              NotificationWebSocketHandler webSocketHandler,
                              ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.auditRepository = auditRepository;
        this.accountMasterRepository = accountMasterRepository;
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * Crea y envía una notificación.
     * 
     * <p>Solo puede ser llamado por servicios que implementan NotificationSender.
     * Valida, sanitiza, persiste, registra auditoría y distribuye en tiempo real.
     * 
     * @param caller el objeto que llama (debe implementar NotificationSender)
     * @param request los datos de la notificación
     * @return la notificación creada
     * @throws UnauthorizedNotificationException si el llamador no está autorizado
     */
    public Notification sendNotification(Object caller, NotificationCreateRequest request) {
        // 1. Validar autorización
        if (!(caller instanceof NotificationSender)) {
            String callerClass = caller != null ? caller.getClass().getName() : "null";
            logger.error("Unauthorized attempt to create notification from: {}", callerClass);
            throw new UnauthorizedNotificationException(
                "Only services implementing NotificationSender can create notifications. " +
                "Caller: " + callerClass
            );
        }

        // 2. Validar y obtener usuario
        AccountMaster user = accountMasterRepository.findByEmail(request.getUserEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserEmail()));

        // 3. Validar frecuencia (rate limiting)
        validateNotificationFrequency(user, request.getType());

        // 4. Sanitizar metadata
        String sanitizedMetadata = sanitizeMetadata(request.getMetadata(), request.getType());

        // 5. Crear notificación
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setMetadata(sanitizedMetadata);
        if (request.getExpiresAt() != null) {
            notification.setExpiresAt(request.getExpiresAt());
        }

        // 6. Persistir
        notification = notificationRepository.save(notification);

        // 7. Registrar auditoría
        registerAudit(caller, notification, sanitizedMetadata);

        // 8. Enviar a través de WebSocket (si el usuario está conectado)
        try {
            webSocketHandler.sendNotificationToUser(user.getEmail(), notification);
        } catch (Exception e) {
            logger.error("Error sending notification via WebSocket: {}", e.getMessage(), e);
            // No fallar la creación si WebSocket falla
        }

        logger.info("Notification created: id={}, type={}, user={}", 
                   notification.getId(), notification.getType(), user.getEmail());

        return notification;
    }

    /**
     * Crea una notificación como administrador.
     * 
     * <p>Este método permite a los administradores crear notificaciones sin
     * requerir que el llamador implemente NotificationSender. Útil para
     * notificaciones administrativas, del sistema, o masivas.
     * 
     * <p>La auditoría registrará que fue creada por un admin.
     * 
     * @param request los datos de la notificación
     * @param adminEmail el email del administrador que crea la notificación
     * @return la notificación creada
     */
    public Notification createNotificationAsAdmin(NotificationCreateRequest request, String adminEmail) {
        // 1. Validar y obtener usuario
        AccountMaster user = accountMasterRepository.findByEmail(request.getUserEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserEmail()));

        // 2. Validar frecuencia (rate limiting)
        validateNotificationFrequency(user, request.getType());

        // 3. Sanitizar metadata
        String sanitizedMetadata = sanitizeMetadata(request.getMetadata(), request.getType());

        // 4. Crear notificación
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setMetadata(sanitizedMetadata);
        if (request.getExpiresAt() != null) {
            notification.setExpiresAt(request.getExpiresAt());
        }

        // 5. Persistir
        notification = notificationRepository.save(notification);

        // 6. Registrar auditoría (marcar como creada por admin)
        try {
            NotificationAuditLog auditLog = new NotificationAuditLog();
            auditLog.setNotification(notification);
            auditLog.setCreatedByService("AdminController");
            auditLog.setCreatedByMethod("createNotificationAsAdmin");
            auditLog.setUserEmail(notification.getUser().getEmail());
            auditLog.setNotificationType(notification.getType());
            auditLog.setMetadataSanitized(sanitizedMetadata);
            // Nota: IP address se puede obtener del contexto de seguridad si es necesario
            auditRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Error registering audit log: {}", e.getMessage(), e);
            // No fallar la creación si la auditoría falla
        }

        // 7. Enviar a través de WebSocket (si el usuario está conectado)
        try {
            webSocketHandler.sendNotificationToUser(user.getEmail(), notification);
        } catch (Exception e) {
            logger.error("Error sending notification via WebSocket: {}", e.getMessage(), e);
            // No fallar la creación si WebSocket falla
        }

        logger.info("Notification created by admin {}: id={}, type={}, user={}", 
                   adminEmail, notification.getId(), notification.getType(), user.getEmail());

        return notification;
    }

    /**
     * Crea y envía una notificación broadcast a todos los usuarios activos.
     * 
     * <p>Este método permite a los administradores enviar notificaciones
     * a todos los usuarios activos (habilitados y con email verificado) del sistema.
     * Útil para notificaciones del sistema, mantenimientos, anuncios, etc.
     * 
     * <p>La auditoría registrará que fue creada por un admin como broadcast.
     * 
     * @param request los datos de la notificación (sin userEmail)
     * @param adminEmail el email del administrador que crea la notificación
     * @return resumen con el número de notificaciones creadas
     */
    public Map<String, Object> createBroadcastNotification(NotificationBroadcastRequest request, String adminEmail) {
        logger.info("Admin {} creating broadcast notification: type={}", adminEmail, request.getType());

        // 1. Obtener todos los usuarios activos
        List<AccountMaster> activeUsers = accountMasterRepository.findByEnabledTrueAndEmailVerifiedTrue();
        
        if (activeUsers.isEmpty()) {
            logger.warn("No active users found for broadcast notification");
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", 0);
            response.put("notificationsCreated", 0);
            response.put("notificationsSentViaWebSocket", 0);
            response.put("message", "No active users found");
            return response;
        }

        logger.info("Broadcasting notification to {} active users", activeUsers.size());

        // 2. Sanitizar metadata una vez (se reutiliza para todos)
        String sanitizedMetadata = sanitizeMetadata(request.getMetadata(), request.getType());

        int notificationsCreated = 0;
        int notificationsSentViaWebSocket = 0;
        List<String> errors = new ArrayList<>();

        // 3. Crear notificación para cada usuario
        for (AccountMaster user : activeUsers) {
            try {
                // Validar frecuencia (rate limiting) - pero más permisivo para broadcast
                // No aplicamos rate limiting estricto en broadcast para evitar bloquear notificaciones del sistema

                // Crear notificación
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setType(request.getType());
                notification.setTitle(request.getTitle());
                notification.setMessage(request.getMessage());
                notification.setMetadata(sanitizedMetadata);
                if (request.getExpiresAt() != null) {
                    notification.setExpiresAt(request.getExpiresAt());
                }

                // Persistir
                notification = notificationRepository.save(notification);
                notificationsCreated++;

                // Registrar auditoría
                try {
                    NotificationAuditLog auditLog = new NotificationAuditLog();
                    auditLog.setNotification(notification);
                    auditLog.setCreatedByService("AdminController");
                    auditLog.setCreatedByMethod("createBroadcastNotification");
                    auditLog.setUserEmail(user.getEmail());
                    auditLog.setNotificationType(notification.getType());
                    auditLog.setMetadataSanitized(sanitizedMetadata);
                    auditRepository.save(auditLog);
                } catch (Exception e) {
                    logger.error("Error registering audit log for user {}: {}", user.getEmail(), e.getMessage());
                    // Continuar aunque falle la auditoría
                }

                // Enviar a través de WebSocket (si el usuario está conectado)
                try {
                    webSocketHandler.sendNotificationToUser(user.getEmail(), notification);
                    notificationsSentViaWebSocket++;
                } catch (Exception e) {
                    logger.warn("Error sending notification via WebSocket to user {}: {}", user.getEmail(), e.getMessage());
                    // Continuar aunque falle WebSocket (la notificación ya está guardada)
                }

            } catch (Exception e) {
                logger.error("Error creating notification for user {}: {}", user.getEmail(), e.getMessage(), e);
                errors.add(user.getEmail() + ": " + e.getMessage());
            }
        }

        logger.info("Broadcast notification completed: created={}, sentViaWebSocket={}, errors={}", 
                   notificationsCreated, notificationsSentViaWebSocket, errors.size());

        // 4. Retornar resumen
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", activeUsers.size());
        response.put("notificationsCreated", notificationsCreated);
        response.put("notificationsSentViaWebSocket", notificationsSentViaWebSocket);
        response.put("errors", errors);
        response.put("message", String.format("Broadcast notification sent to %d users", notificationsCreated));
        
        return response;
    }

    /**
     * Obtiene notificaciones de un usuario.
     */
    public Page<NotificationDTO> getUserNotifications(String userEmail, Pageable pageable) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(NotificationDTO::new);
    }

    /**
     * Obtiene notificaciones no leídas de un usuario.
     */
    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<Notification> notifications = notificationRepository.findUnreadByUser(user);
        return notifications.stream()
            .map(NotificationDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * Marca una notificación como leída.
     */
    public void markAsRead(Long notificationId, String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        // Verificar que la notificación pertenece al usuario
        if (!notification.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas.
     */
    public void markAllAsRead(String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<Notification> unreadNotifications = notificationRepository.findUnreadByUser(user);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Obtiene el conteo de notificaciones no leídas.
     */
    public long getUnreadCount(String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        return notificationRepository.countUnreadByUser(user);
    }

    /**
     * Sanitiza el metadata removiendo datos sensibles.
     */
    private String sanitizeMetadata(Map<String, Object> metadata, NotificationType type) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            // Crear copia para no modificar el original
            Map<String, Object> sanitized = new HashMap<>(metadata);

            // Remover datos sensibles según el tipo
            switch (type) {
                case PAYMENT_SUCCESS:
                case PAYMENT_FAILED:
                    // Permitir: payment_id, amount, provider, terra_coins_added, transaction_id
                    // Remover: cualquier campo que contenga "card", "cvv", "password", "token"
                    sanitized.keySet().removeIf(key -> 
                        key.toLowerCase().contains("card") ||
                        key.toLowerCase().contains("cvv") ||
                        key.toLowerCase().contains("password") ||
                        key.toLowerCase().contains("token") ||
                        key.toLowerCase().contains("secret")
                    );
                    break;
                default:
                    // Para otros tipos, remover campos comunes sensibles
                    sanitized.keySet().removeIf(key -> 
                        key.toLowerCase().contains("password") ||
                        key.toLowerCase().contains("token") ||
                        key.toLowerCase().contains("secret") ||
                        key.toLowerCase().contains("key")
                    );
            }

            // Convertir a JSON string
            return objectMapper.writeValueAsString(sanitized);

        } catch (Exception e) {
            logger.error("Error sanitizing metadata: {}", e.getMessage(), e);
            throw new InvalidMetadataException("Failed to sanitize metadata", e);
        }
    }

    /**
     * Valida la frecuencia de notificaciones (rate limiting).
     */
    private void validateNotificationFrequency(AccountMaster user, NotificationType type) {
        // Esta validación se puede mejorar con Redis para producción
        // Por ahora, validación básica en memoria
        Date oneMinuteAgo = new Date(System.currentTimeMillis() - 60_000);
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3_600_000);

        // Validar máximo por usuario por minuto
        List<Notification> recentNotifications = notificationRepository.findByUserAndCreatedAtAfter(
            user, oneMinuteAgo
        );
        if (recentNotifications.size() >= MAX_NOTIFICATIONS_PER_USER_PER_MINUTE) {
            logger.warn("Rate limit exceeded for user: {} ({} notifications in last minute)", 
                       user.getEmail(), recentNotifications.size());
            // No lanzar excepción, solo loggear (para no interrumpir flujo crítico)
        }

        // Validar máximo del mismo tipo por hora
        long sameTypeCount = recentNotifications.stream()
            .filter(n -> n.getType() == type)
            .count();
        if (sameTypeCount >= MAX_NOTIFICATIONS_SAME_TYPE_PER_HOUR) {
            logger.warn("Rate limit exceeded for user: {} type: {} ({} notifications in last hour)", 
                       user.getEmail(), type, sameTypeCount);
        }
    }

    /**
     * Registra auditoría de la notificación creada.
     */
    private void registerAudit(Object caller, Notification notification, String sanitizedMetadata) {
        try {
            NotificationAuditLog auditLog = new NotificationAuditLog();
            auditLog.setNotification(notification);
            auditLog.setCreatedByService(caller.getClass().getSimpleName());
            auditLog.setCreatedByMethod(getCallerMethodName());
            auditLog.setUserEmail(notification.getUser().getEmail());
            auditLog.setNotificationType(notification.getType());
            auditLog.setMetadataSanitized(sanitizedMetadata);
            // IP address se puede obtener del contexto de seguridad si es necesario

            auditRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Error registering audit log: {}", e.getMessage(), e);
            // No fallar la creación si la auditoría falla
        }
    }

    /**
     * Obtiene el nombre del método que llamó a sendNotification.
     * Usa stack trace para encontrar el método del servicio autorizado.
     */
    private String getCallerMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            // Buscar el primer método que no sea de NotificationService
            if (!className.contains("NotificationService") && 
                !className.contains("java.lang") &&
                !className.contains("sun.reflect")) {
                return element.getMethodName();
            }
        }
        return "unknown";
    }

    /**
     * Elimina todas las notificaciones de un usuario (GDPR - Derecho al Olvido).
     * 
     * @param userEmail el email del usuario
     * @return número de notificaciones eliminadas
     */
    @Transactional
    public long deleteAllUserNotifications(String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        long count = notificationRepository.countByUser(user);
        notificationRepository.deleteByUser(user);
        
        logger.info("Deleted {} notifications for user: {} (GDPR - Right to be forgotten)", count, userEmail);
        return count;
    }

    /**
     * Exporta todas las notificaciones de un usuario en formato JSON (GDPR - Data Portability).
     * 
     * @param userEmail el email del usuario
     * @return lista de notificaciones como DTOs
     */
    public List<NotificationDTO> exportUserNotifications(String userEmail) {
        AccountMaster user = accountMasterRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        
        logger.info("Exported {} notifications for user: {} (GDPR - Data Portability)", 
                   notifications.size(), userEmail);
        
        return notifications.stream()
            .map(NotificationDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * Limpia notificaciones antiguas según políticas de retención.
     * 
     * @return número de notificaciones eliminadas
     */
    @Transactional
    public long cleanupOldNotifications() {
        Date now = new Date();
        long deletedCount = 0;

        // Contar y eliminar notificaciones leídas después de RETENTION_READ_DAYS
        Date readCutoff = new Date(now.getTime() - (RETENTION_READ_DAYS * 24L * 60 * 60 * 1000));
        List<Notification> readToDelete = notificationRepository.findReadNotificationsOlderThan(readCutoff);
        long readDeleted = readToDelete.size();
        if (readDeleted > 0) {
            notificationRepository.deleteReadNotificationsOlderThan(readCutoff);
            deletedCount += readDeleted;
        }

        // Contar y eliminar notificaciones no leídas después de RETENTION_UNREAD_DAYS
        Date unreadCutoff = new Date(now.getTime() - (RETENTION_UNREAD_DAYS * 24L * 60 * 60 * 1000));
        List<Notification> unreadToDelete = notificationRepository.findUnreadNotificationsOlderThan(unreadCutoff);
        long unreadDeleted = unreadToDelete.size();
        if (unreadDeleted > 0) {
            notificationRepository.deleteUnreadNotificationsOlderThan(unreadCutoff);
            deletedCount += unreadDeleted;
        }

        logger.info("Cleanup completed: {} notifications deleted (read: {}, unread: {})", 
                   deletedCount, readDeleted, unreadDeleted);
        
        return deletedCount;
    }

    /**
     * Anonimiza logs de auditoría antiguos hasheando los emails.
     * 
     * @return número de logs anonimizados
     */
    @Transactional
    public long anonymizeOldAuditLogs() {
        Date cutoffDate = new Date(System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)); // 90 días
        List<NotificationAuditLog> oldLogs = auditRepository.findLogsOlderThanForAnonymization(cutoffDate);
        
        int anonymizedCount = 0;
        for (NotificationAuditLog log : oldLogs) {
            if (log.getUserEmail() != null && !log.getUserEmail().startsWith("HASHED_")) {
                // Hashear el email (simple hash para anonimización)
                String hashedEmail = "HASHED_" + Integer.toHexString(log.getUserEmail().hashCode());
                log.setUserEmail(hashedEmail);
                auditRepository.save(log);
                anonymizedCount++;
            }
        }
        
        logger.info("Anonymized {} audit logs older than 90 days", anonymizedCount);
        return anonymizedCount;
    }

}

