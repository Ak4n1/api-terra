package com.ak4n1.terra.api.terra_api.notifications.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Implementación del servicio de envío de notificaciones por email.
 * 
 * <p>Este servicio maneja el envío de emails HTML usando JavaMailSender.
 * Configura los mensajes MIME con remitente, destinatario, asunto y cuerpo HTML.
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
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del email en formato HTML
     */
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            // Crear el mensaje MIME
            MimeMessage message = emailSender.createMimeMessage();

            // Crear el helper para configurar el mensaje (true para que sea en formato HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Establecer el remitente, destinatario, asunto y cuerpo del mensaje
            helper.setFrom(username, "L2 Terra Online");
            helper.setTo(to);
            helper.setSubject(subject);

            // Establecer el cuerpo del mensaje como HTML (true indica que el cuerpo es HTML)
            helper.setText(body, true);

            // Enviar el mensaje
            emailSender.send(message);
            logger.info("📧 [EMAIL] Correo enviado exitosamente a: {}", to);

        } catch (Exception e) {
            logger.error("❌ [EMAIL] Error enviando correo a {}: {}", to, e.getMessage(), e);
        }
    }
} 