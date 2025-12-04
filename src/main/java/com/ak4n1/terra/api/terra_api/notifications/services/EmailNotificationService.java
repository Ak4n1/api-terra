package com.ak4n1.terra.api.terra_api.notifications.services;

import java.util.concurrent.CompletableFuture;

/**
 * Servicio principal para el envío de notificaciones por email.
 * 
 * <p>Este servicio proporciona métodos para enviar emails HTML al sistema de notificaciones.
 * Es el servicio RECOMENDADO para enviar emails desde cualquier parte de la aplicación.
 * 
 * <p>El envío de emails se realiza de forma asíncrona para no bloquear el hilo principal.
 * 
 * @see EmailNotificationServiceImpl
 * @see com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent
 * @author ak4n1
 * @since 1.0
 */
public interface EmailNotificationService {
    
    /**
     * Envía un email HTML a la dirección especificada de forma asíncrona.
     * 
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del email en formato HTML
     * @return CompletableFuture que se completa cuando el email se envía exitosamente
     */
    CompletableFuture<Void> sendEmail(String to, String subject, String body);
} 