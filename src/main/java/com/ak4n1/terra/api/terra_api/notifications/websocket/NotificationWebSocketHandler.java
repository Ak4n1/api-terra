package com.ak4n1.terra.api.terra_api.notifications.websocket;

import com.ak4n1.terra.api.terra_api.notifications.domain.Notification;
import com.ak4n1.terra.api.terra_api.notifications.dto.WebSocketMessage;
import com.ak4n1.terra.api.terra_api.notifications.metrics.WebSocketMetrics;
import com.ak4n1.terra.api.terra_api.notifications.repository.WebSocketSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handler principal para conexiones WebSocket de notificaciones.
 * 
 * <p>Maneja conexiones, desconexiones, mensajes, heartbeat (ping/pong),
 * idle timeout y envío de notificaciones a usuarios conectados.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final WebSocketSessionRepository sessionRepository;
    private final WebSocketSecurityValidator securityValidator;
    private final ObjectMapper objectMapper;
    private final WebSocketMetrics metrics;

    // Mapa de sesiones activas: sessionId -> WebSocketSession de Spring
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Mapa de sesiones por usuario: userEmail -> Set de sessionIds
    private final Map<String, Set<String>> sessionsByUser = new ConcurrentHashMap<>();
    
    // Mapa de última actividad: sessionId -> timestamp
    private final Map<String, Long> lastActivity = new ConcurrentHashMap<>();
    
    // Mapa de tokens por sesión: sessionId -> token
    private final Map<String, String> tokensBySession = new ConcurrentHashMap<>();

    // Configuración
    private static final long HEARTBEAT_INTERVAL_MS = 30_000; // 30 segundos
    private static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutos
    private static final long IDLE_WARNING_MS = 25 * 60 * 1000; // 25 minutos (5 min antes)
    private static final long PING_TIMEOUT_MS = 10_000; // 10 segundos
    private static final int MAX_PING_FAILURES = 3;

    // Scheduler para heartbeat y cleanup
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public NotificationWebSocketHandler(WebSocketSessionRepository sessionRepository,
                                      WebSocketSecurityValidator securityValidator,
                                      ObjectMapper objectMapper,
                                      WebSocketMetrics metrics) {
        this.sessionRepository = sessionRepository;
        this.securityValidator = securityValidator;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        
        // Iniciar tareas programadas
        startHeartbeat();
        startIdleTimeoutChecker();
        startCleanupTask();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String userEmail = (String) session.getAttributes().get("userEmail");
        String token = (String) session.getAttributes().get("token");

        logger.info("🔌 [WebSocket] Connection established - sessionId: {}, userEmail: {}, remoteAddress: {}", 
                sessionId, userEmail, session.getRemoteAddress());

        if (sessionId == null || userEmail == null) {
            logger.error("❌ [WebSocket] Session attributes missing, closing connection. sessionId: {}, userEmail: {}", 
                    sessionId, userEmail);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // Registrar sesión
        activeSessions.put(sessionId, session);
        sessionsByUser.computeIfAbsent(userEmail, k -> new HashSet<>()).add(sessionId);
        lastActivity.put(sessionId, System.currentTimeMillis());
        if (token != null) {
            tokensBySession.put(sessionId, token);
        }

        logger.info("✅ [WebSocket] Connection registered - sessionId: {}, user: {}, totalActiveSessions: {}, userSessions: {}", 
                sessionId, userEmail, activeSessions.size(), sessionsByUser.get(userEmail).size());
        
        // Registrar métrica de conexión establecida
        metrics.incrementSessionsTotal();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String userEmail = (String) session.getAttributes().get("userEmail");

        logger.info("🔌 [WebSocket] Connection closing - sessionId: {}, user: {}, status: {} ({})", 
                sessionId, userEmail, status.getCode(), status.getReason());

        if (sessionId == null || userEmail == null) {
            logger.warn("⚠️ [WebSocket] Session attributes missing on close - sessionId: {}, userEmail: {}", 
                    sessionId, userEmail);
            return;
        }

        // Limpiar sesión
        cleanupSession(sessionId, userEmail);

        logger.info("✅ [WebSocket] Connection closed and cleaned up - sessionId: {}, user: {}, remainingActiveSessions: {}",
                sessionId, userEmail, activeSessions.size());
        
        // Registrar métrica de desconexión
        metrics.decrementActiveSessions();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String userEmail = (String) session.getAttributes().get("userEmail");

        if (sessionId == null) {
            return;
        }

        // Actualizar última actividad
        lastActivity.put(sessionId, System.currentTimeMillis());

        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);

            switch (wsMessage.getType()) {
                case PONG:
                    handlePong(sessionId);
                    break;
                case ACK:
                    handleAck(sessionId, wsMessage.getNotificationId());
                    break;
                case MARK_AS_READ:
                    handleMarkAsRead(sessionId, wsMessage.getNotificationId());
                    break;
                default:
                    logger.warn("Unknown message type received: {}", wsMessage.getType());
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String userEmail = (String) session.getAttributes().get("userEmail");
        
        logger.error("WebSocket transport error: session={}, user={}, error={}", 
                    sessionId, userEmail, exception.getMessage(), exception);
        
        if (sessionId != null && userEmail != null) {
            cleanupSession(sessionId, userEmail);
        }
    }

    /**
     * Envía una notificación a un usuario específico.
     * 
     * @param userEmail el email del usuario
     * @param notification la notificación a enviar
     */
    public void sendNotificationToUser(String userEmail, Notification notification) {
        long startTime = System.currentTimeMillis();
        String notificationType = notification.getType() != null ? notification.getType().name() : "UNKNOWN";
        
        logger.info("📨 [WebSocket] Attempting to send notification to user: {}", userEmail);
        logger.info("📨 [WebSocket] Total active sessions: {}, Sessions by user map size: {}", 
                activeSessions.size(), sessionsByUser.size());
        
        Set<String> sessionIds = sessionsByUser.get(userEmail);
        if (sessionIds == null || sessionIds.isEmpty()) {
            logger.warn("❌ [WebSocket] No active sessions for user: {} (total users with sessions: {})", 
                    userEmail, sessionsByUser.keySet());
            logger.warn("📊 [WebSocket] Active users: {}", sessionsByUser.keySet());
            // Registrar métrica de fallo (no hay sesiones)
            metrics.incrementNotificationsFailed(notificationType);
            return;
        }
        
        logger.info("✅ [WebSocket] Found {} active session(s) for user: {}", sessionIds.size(), userEmail);

        // Crear mensaje WebSocket
        WebSocketMessage wsMessage = createNotificationMessage(notification);

        // Enviar a todas las sesiones del usuario
        List<String> failedSessions = new ArrayList<>();
        int successCount = 0;
        for (String sessionId : sessionIds) {
            WebSocketSession wsSession = activeSessions.get(sessionId);
            if (wsSession != null && wsSession.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(wsMessage);
                    wsSession.sendMessage(new TextMessage(json));
                    successCount++;
                    logger.info("✅ [WebSocket] Notification sent successfully to session: {} (user: {})", 
                            sessionId, userEmail);
                } catch (Exception e) {
                    logger.error("❌ [WebSocket] Error sending notification to session {} (user: {}): {}", 
                            sessionId, userEmail, e.getMessage(), e);
                    failedSessions.add(sessionId);
                }
            } else {
                logger.warn("⚠️ [WebSocket] Session {} not found or closed (user: {})", sessionId, userEmail);
                failedSessions.add(sessionId);
            }
        }
        
        // Registrar métricas
        long deliveryTime = System.currentTimeMillis() - startTime;
        if (successCount > 0) {
            metrics.incrementNotificationsSent(notificationType);
            metrics.recordNotificationDeliveryTime(deliveryTime);
        }
        if (failedSessions.size() > 0) {
            metrics.incrementNotificationsFailed(notificationType);
        }
        
        logger.info("📊 [WebSocket] Notification delivery summary - user: {}, totalSessions: {}, success: {}, failed: {}", 
                userEmail, sessionIds.size(), successCount, failedSessions.size());

        // Limpiar sesiones fallidas
        for (String failedSessionId : failedSessions) {
            cleanupSession(failedSessionId, userEmail);
        }
    }

    /**
     * Crea un mensaje WebSocket a partir de una notificación.
     */
    private WebSocketMessage createNotificationMessage(Notification notification) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.MessageType.NOTIFICATION);
        message.setNotificationId(notification.getId());
        message.setNotificationType(notification.getType());
        message.setTitle(notification.getTitle());
        message.setMessage(notification.getMessage());
        
        // Parsear metadata JSON si existe
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            try {
                Map<String, Object> metadata = objectMapper.readValue(
                    notification.getMetadata(), 
                    Map.class
                );
                message.setMetadata(metadata);
            } catch (Exception e) {
                logger.warn("Error parsing notification metadata: {}", e.getMessage());
            }
        }
        
        return message;
    }

    /**
     * Maneja mensaje PONG del cliente.
     */
    private void handlePong(String sessionId) {
        lastActivity.put(sessionId, System.currentTimeMillis());
        // Validar token si es necesario (para renovación)
        validateTokenIfNeeded(sessionId);
    }

    /**
     * Maneja ACK de notificación.
     */
    private void handleAck(String sessionId, Long notificationId) {
        // Aquí se podría implementar tracking de notificaciones entregadas
        logger.debug("ACK received for notification {} from session {}", notificationId, sessionId);
    }

    /**
     * Maneja solicitud de marcar notificación como leída.
     */
    private void handleMarkAsRead(String sessionId, Long notificationId) {
        // Esta funcionalidad se implementará en el servicio de notificaciones
        logger.debug("Mark as read requested for notification {} from session {}", notificationId, sessionId);
    }

    /**
     * Valida el token si está próximo a expirar.
     */
    private void validateTokenIfNeeded(String sessionId) {
        String token = tokensBySession.get(sessionId);
        if (token == null) {
            return;
        }

        try {
            // Validar token (esto también verifica expiración)
            securityValidator.validateToken(token);
        } catch (SecurityException e) {
            if (e.getMessage().contains("expired") || e.getMessage().contains("expiring")) {
                sendTokenRefreshRequired(sessionId);
            }
        }
    }

    /**
     * Envía mensaje de requerimiento de refresh de token.
     */
    private void sendTokenRefreshRequired(String sessionId) {
        WebSocketSession wsSession = activeSessions.get(sessionId);
        if (wsSession == null || !wsSession.isOpen()) {
            return;
        }

        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType(WebSocketMessage.MessageType.TOKEN_REFRESH_REQUIRED);
            String json = objectMapper.writeValueAsString(message);
            wsSession.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            logger.error("Error sending token refresh required: {}", e.getMessage());
        }
    }

    /**
     * Limpia una sesión (remueve de todos los mapas y marca como desconectada en BD).
     */
    private void cleanupSession(String sessionId, String userEmail) {
        activeSessions.remove(sessionId);
        sessionsByUser.computeIfPresent(userEmail, (k, v) -> {
            v.remove(sessionId);
            return v.isEmpty() ? null : v;
        });
        lastActivity.remove(sessionId);
        tokensBySession.remove(sessionId);

        // Marcar como desconectada en BD
        sessionRepository.findBySessionId(sessionId).ifPresent(wsSessionEntity -> {
            wsSessionEntity.markAsDisconnected();
            sessionRepository.save(wsSessionEntity);
        });

        // Decrementar contador de conexiones activas
        securityValidator.decrementActiveConnections(userEmail);
    }

    /**
     * Inicia el heartbeat (ping cada 30 segundos).
     */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            List<String> sessionsToClose = new ArrayList<>();

            for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
                String sessionId = entry.getKey();
                WebSocketSession wsSession = entry.getValue();

                if (!wsSession.isOpen()) {
                    sessionsToClose.add(sessionId);
                    continue;
                }

                try {
                    WebSocketMessage ping = new WebSocketMessage();
                    ping.setType(WebSocketMessage.MessageType.PING);
                    String json = objectMapper.writeValueAsString(ping);
                    wsSession.sendMessage(new TextMessage(json));
                } catch (Exception e) {
                    logger.error("Error sending ping to session {}: {}", sessionId, e.getMessage());
                    sessionsToClose.add(sessionId);
                }
            }

            // Cerrar sesiones con errores
            for (String sessionId : sessionsToClose) {
                String userEmail = sessionsByUser.entrySet().stream()
                    .filter(e -> e.getValue().contains(sessionId))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
                if (userEmail != null) {
                    cleanupSession(sessionId, userEmail);
                }
            }
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Inicia el checker de idle timeout.
     */
    private void startIdleTimeoutChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            List<String> sessionsToClose = new ArrayList<>();

            for (Map.Entry<String, Long> entry : lastActivity.entrySet()) {
                String sessionId = entry.getKey();
                long lastActivityTime = entry.getValue();
                long idleTime = now - lastActivityTime;

                // Advertencia 5 minutos antes
                if (idleTime >= IDLE_WARNING_MS && idleTime < IDLE_TIMEOUT_MS) {
                    sendIdleTimeoutWarning(sessionId);
                }

                // Cerrar si excede timeout
                if (idleTime >= IDLE_TIMEOUT_MS) {
                    sessionsToClose.add(sessionId);
                }
            }

            // Cerrar sesiones inactivas
            for (String sessionId : sessionsToClose) {
                String userEmail = sessionsByUser.entrySet().stream()
                    .filter(e -> e.getValue().contains(sessionId))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
                if (userEmail != null) {
                    closeSessionWithIdleTimeout(sessionId, userEmail);
                }
            }
        }, 60_000, 60_000, TimeUnit.MILLISECONDS); // Check cada minuto
    }

    /**
     * Envía advertencia de idle timeout.
     */
    private void sendIdleTimeoutWarning(String sessionId) {
        WebSocketSession wsSession = activeSessions.get(sessionId);
        if (wsSession == null || !wsSession.isOpen()) {
            return;
        }

        try {
            WebSocketMessage message = new WebSocketMessage();
            message.setType(WebSocketMessage.MessageType.IDLE_TIMEOUT);
            String json = objectMapper.writeValueAsString(message);
            wsSession.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            logger.error("Error sending idle timeout warning: {}", e.getMessage());
        }
    }

    /**
     * Cierra sesión por idle timeout.
     */
    private void closeSessionWithIdleTimeout(String sessionId, String userEmail) {
        WebSocketSession wsSession = activeSessions.get(sessionId);
        if (wsSession != null && wsSession.isOpen()) {
            try {
                wsSession.close(CloseStatus.GOING_AWAY);
            } catch (IOException e) {
                logger.error("Error closing idle session: {}", e.getMessage());
            }
        }
        cleanupSession(sessionId, userEmail);
    }

    /**
     * Inicia tarea de limpieza periódica.
     */
    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            securityValidator.cleanupExpiredEntries();
        }, 5 * 60_000, 5 * 60_000, TimeUnit.MILLISECONDS); // Cada 5 minutos
    }

    /**
     * Cierra todas las conexiones (para graceful shutdown).
     */
    public void closeAllConnections() {
        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            try {
                WebSocketMessage message = new WebSocketMessage();
                message.setType(WebSocketMessage.MessageType.SERVER_SHUTDOWN);
                message.setReconnectInSeconds(30);
                String json = objectMapper.writeValueAsString(message);
                entry.getValue().sendMessage(new TextMessage(json));
                Thread.sleep(100); // Pequeña pausa entre mensajes
            } catch (Exception e) {
                logger.error("Error sending shutdown message: {}", e.getMessage());
            }
        }

        // Cerrar todas las conexiones después de 2 segundos
        scheduler.schedule(() -> {
            for (WebSocketSession wsSession : activeSessions.values()) {
                try {
                    if (wsSession.isOpen()) {
                        wsSession.close(CloseStatus.SERVER_ERROR);
                    }
                } catch (Exception e) {
                    logger.error("Error closing session during shutdown: {}", e.getMessage());
                }
            }
        }, 2, TimeUnit.SECONDS);
    }
}

