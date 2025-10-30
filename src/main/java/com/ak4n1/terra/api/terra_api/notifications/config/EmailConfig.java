package com.ak4n1.terra.api.terra_api.notifications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

/**
 * Configuración de Spring para el servicio de emails.
 * 
 * <p>Esta clase configura el JavaMailSender con las propiedades de SMTP necesarias
 * para enviar emails. Obtiene la configuración desde application.properties.
 * 
 * @see JavaMailSender
 * @see com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService
 * @author ak4n1
 * @since 1.0
 */
@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    /**
     * Crea y configura el bean JavaMailSender para el envío de emails.
     * 
     * <p>Configura las propiedades SMTP necesarias incluyendo autenticación
     * y SSL/TLS según la configuración del servidor de correo.
     * 
     * @return JavaMailSender configurado con las propiedades SMTP
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", host);

        return mailSender;
    }
} 