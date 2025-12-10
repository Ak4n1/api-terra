package com.ak4n1.terra.api.terra_api.notifications.repository;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Repository para notificaciones.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Buscar notificaciones por usuario, ordenadas por fecha de creación descendente.
     * 
     * @param user el usuario
     * @return lista de notificaciones
     */
    List<Notification> findByUserOrderByCreatedAtDesc(AccountMaster user);

    /**
     * Buscar notificaciones por usuario con paginación.
     * 
     * @param user el usuario
     * @param pageable configuración de paginación
     * @return página de notificaciones
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(AccountMaster user, Pageable pageable);

    /**
     * Buscar notificaciones no leídas por usuario.
     * 
     * @param user el usuario
     * @return lista de notificaciones no leídas
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUser(@Param("user") AccountMaster user);

    /**
     * Buscar notificaciones no leídas por usuario con paginación.
     * 
     * @param user el usuario
     * @param pageable configuración de paginación
     * @return página de notificaciones no leídas
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUser(@Param("user") AccountMaster user, Pageable pageable);

    /**
     * Contar notificaciones no leídas por usuario.
     * 
     * @param user el usuario
     * @return número de notificaciones no leídas
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.readAt IS NULL")
    long countUnreadByUser(@Param("user") AccountMaster user);

    /**
     * Buscar notificaciones por tipo.
     * 
     * @param type el tipo de notificación
     * @return lista de notificaciones
     */
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    /**
     * Buscar notificaciones por usuario y tipo.
     * 
     * @param user el usuario
     * @param type el tipo de notificación
     * @return lista de notificaciones
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(AccountMaster user, NotificationType type);

    /**
     * Buscar notificaciones no leídas creadas después de una fecha específica.
     * Útil para sincronización cuando el usuario se reconecta.
     * 
     * @param user el usuario
     * @param since fecha desde la cual buscar
     * @return lista de notificaciones no leídas
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NULL AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findUnreadSince(@Param("user") AccountMaster user, @Param("since") Date since);

    /**
     * Buscar notificaciones expiradas.
     * 
     * @param now fecha actual
     * @return lista de notificaciones expiradas
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    List<Notification> findExpired(@Param("now") Date now);

    /**
     * Contar todas las notificaciones por usuario.
     * 
     * @param user el usuario
     * @return número total de notificaciones
     */
    long countByUser(AccountMaster user);

    /**
     * Eliminar notificaciones expiradas.
     * 
     * @param now fecha actual
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    void deleteExpired(@Param("now") Date now);

    /**
     * Buscar notificaciones por usuario creadas después de una fecha.
     * 
     * @param user el usuario
     * @param date fecha desde la cual buscar
     * @return lista de notificaciones
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt > :date ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndCreatedAtAfter(@Param("user") AccountMaster user, @Param("date") Date date);

    /**
     * Eliminar todas las notificaciones de un usuario (GDPR - Derecho al Olvido).
     * 
     * @param user el usuario
     */
    void deleteByUser(AccountMaster user);

    /**
     * Buscar notificaciones leídas más antiguas que una fecha (para retención automática).
     * 
     * @param cutoffDate fecha límite
     * @return lista de notificaciones a eliminar
     */
    @Query("SELECT n FROM Notification n WHERE n.readAt IS NOT NULL AND n.readAt < :cutoffDate")
    List<Notification> findReadNotificationsOlderThan(@Param("cutoffDate") Date cutoffDate);

    /**
     * Buscar notificaciones no leídas más antiguas que una fecha (para retención automática).
     * 
     * @param cutoffDate fecha límite
     * @return lista de notificaciones a eliminar
     */
    @Query("SELECT n FROM Notification n WHERE n.readAt IS NULL AND n.createdAt < :cutoffDate")
    List<Notification> findUnreadNotificationsOlderThan(@Param("cutoffDate") Date cutoffDate);

    /**
     * Eliminar notificaciones leídas más antiguas que una fecha.
     * 
     * @param cutoffDate fecha límite
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.readAt IS NOT NULL AND n.readAt < :cutoffDate")
    void deleteReadNotificationsOlderThan(@Param("cutoffDate") Date cutoffDate);

    /**
     * Eliminar notificaciones no leídas más antiguas que una fecha.
     * 
     * @param cutoffDate fecha límite
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.readAt IS NULL AND n.createdAt < :cutoffDate")
    void deleteUnreadNotificationsOlderThan(@Param("cutoffDate") Date cutoffDate);
}

