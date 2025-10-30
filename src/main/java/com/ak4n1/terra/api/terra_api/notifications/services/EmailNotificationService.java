package com.ak4n1.terra.api.terra_api.notifications.services;

/**
 * Servicio principal para el envío de notificaciones por email.
 * 
 * <p>Este servicio proporciona métodos para enviar emails HTML al sistema de notificaciones.
 * Es el servicio RECOMENDADO para enviar emails desde cualquier parte de la aplicación.
 * 
 * @see EmailNotificationServiceImpl
 * @see com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent
 * @author ak4n1
 * @since 1.0
 */
public interface EmailNotificationService {
    
    /**
     * Envía un email HTML a la dirección especificada.
     * 
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del email en formato HTML
     */
    void sendEmail(String to, String subject, String body);
} 