package com.ak4n1.terra.api.terra_api.notifications.repository;

import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationAuditLog;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository para logs de auditoría de notificaciones.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAuditLog, Long> {

    /**
     * Buscar logs de auditoría por notificación.
     * 
     * @param notification la notificación
     * @return lista de logs de auditoría
     */
    List<NotificationAuditLog> findByNotificationOrderByCreatedAtDesc(Notification notification);

    /**
     * Buscar logs de auditoría por email de usuario.
     * 
     * @param userEmail el email del usuario
     * @return lista de logs de auditoría
     */
    List<NotificationAuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    /**
     * Buscar logs de auditoría por servicio que creó la notificación.
     * 
     * @param serviceName el nombre del servicio
     * @return lista de logs de auditoría
     */
    List<NotificationAuditLog> findByCreatedByServiceOrderByCreatedAtDesc(String serviceName);

    /**
     * Buscar logs de auditoría por tipo de notificación.
     * 
     * @param type el tipo de notificación
     * @return lista de logs de auditoría
     */
    List<NotificationAuditLog> findByNotificationTypeOrderByCreatedAtDesc(NotificationType type);

    /**
     * Buscar logs de auditoría por rango de fechas.
     * 
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @return lista de logs de auditoría
     */
    @Query("SELECT nal FROM NotificationAuditLog nal WHERE nal.createdAt BETWEEN :startDate AND :endDate ORDER BY nal.createdAt DESC")
    List<NotificationAuditLog> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Buscar logs de auditoría por email de usuario con paginación.
     * 
     * @param userEmail el email del usuario
     * @param pageable configuración de paginación
     * @return página de logs de auditoría
     */
    Page<NotificationAuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    /**
     * Contar logs de auditoría por servicio.
     * 
     * @param serviceName el nombre del servicio
     * @return número de logs
     */
    long countByCreatedByService(String serviceName);

    /**
     * Contar logs de auditoría por email de usuario.
     * 
     * @param userEmail el email del usuario
     * @return número de logs
     */
    long countByUserEmail(String userEmail);

    /**
     * Buscar logs de auditoría más antiguos que una fecha (para anonimización).
     * 
     * @param cutoffDate fecha límite
     * @return lista de logs a anonimizar
     */
    @Query("SELECT nal FROM NotificationAuditLog nal WHERE nal.createdAt < :cutoffDate AND nal.userEmail NOT LIKE 'HASHED_%'")
    List<NotificationAuditLog> findLogsOlderThanForAnonymization(@Param("cutoffDate") Date cutoffDate);
}

