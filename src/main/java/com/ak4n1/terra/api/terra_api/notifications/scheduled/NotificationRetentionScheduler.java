package com.ak4n1.terra.api.terra_api.notifications.scheduled;

import com.ak4n1.terra.api.terra_api.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job programado para limpieza automática de notificaciones según políticas de retención.
 * 
 * <p>Ejecuta tareas de mantenimiento:
 * <ul>
 *   <li>Limpieza de notificaciones antiguas (diariamente a las 2:00 AM)</li>
 *   <li>Anonimización de logs de auditoría antiguos (semanalmente los domingos a las 3:00 AM)</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class NotificationRetentionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRetentionScheduler.class);

    private final NotificationService notificationService;

    public NotificationRetentionScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Limpia notificaciones antiguas según políticas de retención.
     * Ejecuta diariamente a las 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Diariamente a las 2:00 AM
    public void cleanupOldNotifications() {
        logger.info("🧹 [Scheduler] Starting notification cleanup job...");
        try {
            long deletedCount = notificationService.cleanupOldNotifications();
            logger.info("✅ [Scheduler] Notification cleanup completed: {} notifications deleted", deletedCount);
        } catch (Exception e) {
            logger.error("❌ [Scheduler] Error during notification cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Anonimiza logs de auditoría antiguos hasheando emails.
     * Ejecuta semanalmente los domingos a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 ? * SUN") // Domingos a las 3:00 AM
    public void anonymizeOldAuditLogs() {
        logger.info("🔒 [Scheduler] Starting audit log anonymization job...");
        try {
            long anonymizedCount = notificationService.anonymizeOldAuditLogs();
            logger.info("✅ [Scheduler] Audit log anonymization completed: {} logs anonymized", anonymizedCount);
        } catch (Exception e) {
            logger.error("❌ [Scheduler] Error during audit log anonymization: {}", e.getMessage(), e);
        }
    }
}

