package com.ak4n1.terra.api.terra_api;

import com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EmailTest {

    @Autowired
    private EmailNotificationService emailService;

    @Test
    public void testEmailConfiguration() {
        try {
            // Test simple para verificar la configuración
            String testEmail = "test@example.com"; // Cambia por tu email de prueba
            String subject = "Test de Configuración - L2 Terra";
            String body = "<h1>Test de Email</h1><p>Si recibes este email, la configuración está funcionando correctamente.</p>";
            
            emailService.sendEmail(testEmail, subject, body);
            System.out.println("✅ Test de email enviado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error en test de email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

