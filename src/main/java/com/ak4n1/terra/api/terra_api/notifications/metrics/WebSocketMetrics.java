package com.ak4n1.terra.api.terra_api.notifications.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Componente para registrar métricas de WebSocket y notificaciones.
 * 
 * <p>Expone métricas que pueden ser consumidas por Spring Boot Actuator
 * y visualizadas en dashboards (Prometheus, Grafana, etc.).
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class WebSocketMetrics {

    private final MeterRegistry meterRegistry;
    
    // Contadores
    private final Counter sessionsTotal;
    private final Counter sessionsRejected;
    private final Counter notificationsSent;
    private final Counter notificationsFailed;
    private final Counter reconnections;
    
    // Timers
    private final Timer notificationDeliveryTime;
    
    // Gauges (valores actuales)
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicInteger notificationsInQueue = new AtomicInteger(0);

    public WebSocketMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Inicializar contadores
        this.sessionsTotal = Counter.builder("websocket.sessions.total")
                .description("Total number of WebSocket connections established")
                .register(meterRegistry);
        
        this.sessionsRejected = Counter.builder("websocket.sessions.rejected")
                .description("Number of WebSocket connection attempts rejected")
                .tag("reason", "unknown")
                .register(meterRegistry);
        
        this.notificationsSent = Counter.builder("websocket.notifications.sent")
                .description("Total number of notifications sent via WebSocket")
                .register(meterRegistry);
        
        this.notificationsFailed = Counter.builder("websocket.notifications.failed")
                .description("Number of notifications that failed to be delivered")
                .register(meterRegistry);
        
        this.reconnections = Counter.builder("websocket.reconnections")
                .description("Number of WebSocket reconnection attempts")
                .register(meterRegistry);
        
        // Inicializar timer
        this.notificationDeliveryTime = Timer.builder("websocket.notifications.delivery.time")
                .description("Time taken to deliver notifications via WebSocket")
                .register(meterRegistry);
        
        // Registrar gauges
        Gauge.builder("websocket.sessions.active", activeSessions, AtomicInteger::get)
                .description("Current number of active WebSocket sessions")
                .register(meterRegistry);
        
        Gauge.builder("websocket.notifications.queue.size", notificationsInQueue, AtomicInteger::get)
                .description("Current number of notifications in queue")
                .register(meterRegistry);
    }

    /**
     * Incrementa el contador de sesiones totales establecidas.
     */
    public void incrementSessionsTotal() {
        sessionsTotal.increment();
        activeSessions.incrementAndGet();
    }

    /**
     * Incrementa el contador de sesiones rechazadas.
     * 
     * @param reason razón del rechazo (token_invalid, origin_invalid, rate_limit, etc.)
     */
    public void incrementSessionsRejected(String reason) {
        Counter.builder("websocket.sessions.rejected")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        sessionsRejected.increment();
    }

    /**
     * Decrementa el contador de sesiones activas.
     */
    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }

    /**
     * Incrementa el contador de notificaciones enviadas.
     * 
     * @param notificationType tipo de notificación
     */
    public void incrementNotificationsSent(String notificationType) {
        Counter.builder("websocket.notifications.sent")
                .tag("type", notificationType)
                .register(meterRegistry)
                .increment();
        notificationsSent.increment();
    }

    /**
     * Incrementa el contador de notificaciones fallidas.
     * 
     * @param notificationType tipo de notificación
     */
    public void incrementNotificationsFailed(String notificationType) {
        Counter.builder("websocket.notifications.failed")
                .tag("type", notificationType)
                .register(meterRegistry)
                .increment();
        notificationsFailed.increment();
    }

    /**
     * Registra el tiempo de entrega de una notificación.
     * 
     * @param durationMs duración en milisegundos
     */
    public void recordNotificationDeliveryTime(long durationMs) {
        notificationDeliveryTime.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Incrementa el contador de reconexiones.
     */
    public void incrementReconnections() {
        reconnections.increment();
    }

    /**
     * Obtiene el número actual de sesiones activas.
     */
    public int getActiveSessions() {
        return activeSessions.get();
    }

    /**
     * Establece el número de sesiones activas (útil para sincronización).
     */
    public void setActiveSessions(int count) {
        activeSessions.set(count);
    }
}

