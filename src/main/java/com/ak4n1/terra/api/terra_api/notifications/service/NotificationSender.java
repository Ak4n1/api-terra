package com.ak4n1.terra.api.terra_api.notifications.service;

/**
 * Interfaz que marca servicios autorizados para crear notificaciones.
 * 
 * <p>Los servicios que implementen esta interfaz pueden crear notificaciones
 * a través del NotificationService. Los controladores REST no pueden crear
 * notificaciones directamente.
 * 
 * @author ak4n1
 * @since 1.0
 */
public interface NotificationSender {
    // Marker interface - no métodos requeridos
}

