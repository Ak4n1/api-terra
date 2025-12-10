package com.ak4n1.terra.api.terra_api.notifications.repository;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.notifications.domain.WebSocketSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository para sesiones WebSocket.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface WebSocketSessionRepository extends JpaRepository<WebSocketSession, Long> {

    /**
     * Buscar sesión por session ID.
     * 
     * @param sessionId el ID de la sesión
     * @return sesión encontrada
     */
    Optional<WebSocketSession> findBySessionId(String sessionId);

    /**
     * Buscar sesiones activas por usuario (no desconectadas).
     * 
     * @param user el usuario
     * @return lista de sesiones activas
     */
    @Query("SELECT ws FROM WebSocketSession ws WHERE ws.user = :user AND ws.disconnectedAt IS NULL ORDER BY ws.connectedAt DESC")
    List<WebSocketSession> findActiveByUser(@Param("user") AccountMaster user);

    /**
     * Buscar sesiones activas por email de usuario.
     * 
     * @param userEmail el email del usuario
     * @return lista de sesiones activas
     */
    @Query("SELECT ws FROM WebSocketSession ws WHERE ws.user.email = :userEmail AND ws.disconnectedAt IS NULL ORDER BY ws.connectedAt DESC")
    List<WebSocketSession> findActiveByUserEmail(@Param("userEmail") String userEmail);

    /**
     * Contar sesiones activas por usuario.
     * 
     * @param user el usuario
     * @return número de sesiones activas
     */
    @Query("SELECT COUNT(ws) FROM WebSocketSession ws WHERE ws.user = :user AND ws.disconnectedAt IS NULL")
    long countActiveByUser(@Param("user") AccountMaster user);

    /**
     * Buscar sesiones inactivas (desconectadas) por usuario.
     * 
     * @param user el usuario
     * @return lista de sesiones inactivas
     */
    @Query("SELECT ws FROM WebSocketSession ws WHERE ws.user = :user AND ws.disconnectedAt IS NOT NULL ORDER BY ws.disconnectedAt DESC")
    List<WebSocketSession> findInactiveByUser(@Param("user") AccountMaster user);

    /**
     * Buscar sesiones expiradas (sin actividad reciente).
     * 
     * @param expirationTime tiempo de expiración
     * @return lista de sesiones expiradas
     */
    @Query("SELECT ws FROM WebSocketSession ws WHERE ws.lastActivity < :expirationTime AND ws.disconnectedAt IS NULL")
    List<WebSocketSession> findExpiredSessions(@Param("expirationTime") Date expirationTime);

    /**
     * Marcar sesiones expiradas como desconectadas.
     * 
     * @param expirationTime tiempo de expiración
     * @return número de sesiones actualizadas
     */
    @Modifying
    @Query("UPDATE WebSocketSession ws SET ws.disconnectedAt = :now WHERE ws.lastActivity < :expirationTime AND ws.disconnectedAt IS NULL")
    int markExpiredSessionsAsDisconnected(@Param("expirationTime") Date expirationTime, @Param("now") Date now);

    /**
     * Actualizar última actividad de una sesión.
     * 
     * @param sessionId el ID de la sesión
     * @param lastActivity nueva fecha de última actividad
     */
    @Modifying
    @Query("UPDATE WebSocketSession ws SET ws.lastActivity = :lastActivity WHERE ws.sessionId = :sessionId")
    void updateLastActivity(@Param("sessionId") String sessionId, @Param("lastActivity") Date lastActivity);

    /**
     * Marcar sesión como desconectada.
     * 
     * @param sessionId el ID de la sesión
     * @param disconnectedAt fecha de desconexión
     */
    @Modifying
    @Query("UPDATE WebSocketSession ws SET ws.disconnectedAt = :disconnectedAt WHERE ws.sessionId = :sessionId")
    void markAsDisconnected(@Param("sessionId") String sessionId, @Param("disconnectedAt") Date disconnectedAt);

    /**
     * Eliminar sesiones antiguas (más de 30 días desde desconexión).
     * 
     * @param cutoffDate fecha límite
     */
    @Modifying
    @Query("DELETE FROM WebSocketSession ws WHERE ws.disconnectedAt IS NOT NULL AND ws.disconnectedAt < :cutoffDate")
    void deleteOldSessions(@Param("cutoffDate") Date cutoffDate);
}

