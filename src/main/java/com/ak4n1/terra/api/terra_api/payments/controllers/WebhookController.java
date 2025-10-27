package com.ak4n1.terra.api.terra_api.payments.controllers;

import com.ak4n1.terra.api.terra_api.payments.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para webhooks de Mercado Pago
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;
    
    /**
     * Webhook de Mercado Pago
     */
    @PostMapping(value = "/webhook", consumes = {"application/json", "text/plain"})
    public ResponseEntity<Map<String, String>> handleMercadoPagoWebhook(
            HttpServletRequest request,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        logger.info("🟢 [WEBHOOK] ===== WEBHOOK RECIBIDO =====");
        logger.info("🟢 [WEBHOOK] IP Remota: {}", request.getRemoteAddr());
        logger.info("🟢 [WEBHOOK] User-Agent: {}", request.getHeader("User-Agent"));
        logger.info("🟢 [WEBHOOK] Content-Type: {}", request.getContentType());
        logger.info("🟢 [WEBHOOK] Content-Length: {}", request.getContentLength());
        logger.info("🟢 [WEBHOOK] Signature: {}", signature);
        
        try {
            // Leer el payload del request
            String payload = readRequestBody(request);
            
            logger.info("🟢 [WEBHOOK] Payload completo: {}", payload);
            logger.info("🟢 [WEBHOOK] Tamaño del payload: {} caracteres", payload.length());
            
            // Verificar firma del webhook (opcional para desarrollo)
            if (signature != null && !signature.isEmpty()) {
                logger.info("🔐 [WEBHOOK] Verificando firma del webhook");
                if (!verifyWebhookSignature(payload, signature)) {
                    logger.warn("⚠️ [WEBHOOK] Firma del webhook no válida");
                    // En desarrollo, continuar aunque la firma no sea válida
                    logger.info("🟡 [WEBHOOK] Continuando en modo desarrollo (firma ignorada)");
                } else {
                    logger.info("✅ [WEBHOOK] Firma del webhook verificada correctamente");
                }
            } else {
                logger.info("🟡 [WEBHOOK] No se recibió firma del webhook (modo desarrollo)");
            }
            
            // Procesar el webhook
            logger.info("🟢 [WEBHOOK] Llamando a paymentService.processMercadoPagoWebhook");
            boolean success = paymentService.processMercadoPagoWebhook(payload, signature);
            
            Map<String, String> response = new HashMap<>();
            if (success) {
                logger.info("✅ [WEBHOOK] Webhook procesado exitosamente");
                response.put("status", "success");
                response.put("message", "Webhook procesado correctamente");
                return ResponseEntity.ok(response);
            } else {
                logger.error("❌ [WEBHOOK] Error al procesar webhook");
                response.put("status", "error");
                response.put("message", "Error al procesar webhook");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ [WEBHOOK] Error al procesar webhook de Mercado Pago: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint de prueba para webhooks
     */
    @PostMapping("/webhook/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@RequestBody String payload) {
        try {
            logger.info("Webhook de prueba recibido");
            logger.info("Payload: {}", payload);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook de prueba procesado correctamente");
            response.put("payload", payload);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error en webhook de prueba: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error en webhook de prueba");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint para simular webhook de Mercado Pago (solo para testing)
     */
    @PostMapping("/webhook/simulate")
    public ResponseEntity<Map<String, Object>> simulateMercadoPagoWebhook(
            @RequestParam(value = "paymentId", required = false) String paymentId,
            @RequestParam(value = "preferenceId", required = false) String preferenceId,
            @RequestParam(value = "status", defaultValue = "approved") String status) {
        
        try {
            logger.info("🎭 [SIMULATE] Simulando webhook de Mercado Pago");
            logger.info("🎭 [SIMULATE] Payment ID: {}", paymentId);
            logger.info("🎭 [SIMULATE] Preference ID: {}", preferenceId);
            logger.info("🎭 [SIMULATE] Status: {}", status);
            
            // Crear payload simulado de Mercado Pago
            String simulatedPayload = String.format(
                "{\"resource\":\"%s\",\"topic\":\"payment\"}",
                paymentId != null ? paymentId : "123456789"
            );
            
            logger.info("🎭 [SIMULATE] Payload simulado: {}", simulatedPayload);
            
            // Procesar el webhook simulado
            boolean success = paymentService.processMercadoPagoWebhook(simulatedPayload, "simulated-signature");
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                logger.info("✅ [SIMULATE] Webhook simulado procesado exitosamente");
                response.put("status", "success");
                response.put("message", "Webhook simulado procesado correctamente");
                response.put("paymentId", paymentId);
                response.put("preferenceId", preferenceId);
                response.put("status", status);
            } else {
                logger.error("❌ [SIMULATE] Error al procesar webhook simulado");
                response.put("status", "error");
                response.put("message", "Error al procesar webhook simulado");
            }
            
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [SIMULATE] Error en webhook simulado: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error en webhook simulado");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint para verificar que el webhook está funcionando
     */
    @GetMapping("/webhook/health")
    public ResponseEntity<Map<String, Object>> webhookHealth(HttpServletRequest request) {
        logger.info("🏥 [HEALTH] Verificación de salud del webhook");
        logger.info("🏥 [HEALTH] IP Remota: {}", request.getRemoteAddr());
        logger.info("🏥 [HEALTH] User-Agent: {}", request.getHeader("User-Agent"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("message", "Webhook endpoint funcionando correctamente");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "Terra API Payments");
        response.put("version", "1.0.0");
        response.put("environment", "production");
        
        logger.info("✅ [HEALTH] Webhook saludable - respuesta enviada");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verificar la firma del webhook de Mercado Pago
     */
    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            // Mercado Pago usa SHA256 con la clave secreta
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String dataToHash = payload + webhookSecret;
            byte[] hash = digest.digest(dataToHash.getBytes("UTF-8"));
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String expectedSignature = hexString.toString();
            boolean isValid = signature.equals(expectedSignature);
            
            logger.info("🔐 [WEBHOOK] Firma esperada: {}", expectedSignature);
            logger.info("🔐 [WEBHOOK] Firma recibida: {}", signature);
            logger.info("🔐 [WEBHOOK] Firma válida: {}", isValid);
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("❌ [WEBHOOK] Error verificando firma: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Leer el cuerpo del request como String
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        
        try {
            bufferedReader = request.getReader();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        
        return stringBuilder.toString();
    }
}
