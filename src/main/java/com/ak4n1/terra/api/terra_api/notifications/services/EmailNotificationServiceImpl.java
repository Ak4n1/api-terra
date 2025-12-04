package com.ak4n1.terra.api.terra_api.notifications.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación del servicio de envío de notificaciones por email.
 * 
 * <p>Este servicio maneja el envío de emails HTML usando JavaMailSender.
 * Configura los mensajes MIME con remitente, destinatario, asunto y cuerpo HTML.
 * 
 * <p>El envío se realiza de forma asíncrona para no bloquear el hilo principal de la aplicación.
 * 
 * @see EmailNotificationService
 * @see JavaMailSender
 * @see com.ak4n1.terra.api.terra_api.notifications.config.EmailConfig
 * @author ak4n1
 * @since 1.0
 */
@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    private JavaMailSender emailSender;

    /**
     * {@inheritDoc}
     * 
     * <p>Envía el email de forma asíncrona. No bloquea el hilo principal.
     * 
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del email en formato HTML
     * @return CompletableFuture que se completa cuando el email se envía exitosamente o falla
     */
    @Override
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String body) {
        try {
            
            // Crear el mensaje MIME
            MimeMessage message = emailSender.createMimeMessage();

            // Crear el helper para configurar el mensaje (true para que sea en formato HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Establecer el remitente, destinatario, asunto y cuerpo del mensaje
            helper.setFrom(username, "L2 Terra");
            helper.setReplyTo(username, "L2 Terra Online Support");
            helper.setTo(to);
            helper.setSubject(subject);

            // Establecer el cuerpo del mensaje como HTML (true indica que el cuerpo es HTML)
            helper.setText(body, true);
            
            // Headers anti-spam para mejorar deliverabilidad
            // X-Mailer: Identifica el software que envía el email
            message.setHeader("X-Mailer", "L2 Terra Online - JavaMail");
            // X-Auto-Response-Suppress: Suprime respuestas automáticas
            message.setHeader("X-Auto-Response-Suppress", "All");
            // X-Priority: Prioridad normal (1 = alta, 3 = normal, 5 = baja)
            message.setHeader("X-Priority", "3");
            // List-Unsubscribe: Permite desuscribirse fácilmente (requisito CAN-SPAM)
            message.setHeader("List-Unsubscribe", "<https://l2terra.online>, <mailto:" + username + "?subject=unsubscribe>");
            message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

            // Enviar el mensaje
            emailSender.send(message);
            
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            logger.error("❌ [EMAIL] Error enviando correo a {}: {}", to, e.getMessage(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
} 