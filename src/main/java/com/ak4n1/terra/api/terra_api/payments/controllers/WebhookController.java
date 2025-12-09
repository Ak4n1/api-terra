package com.ak4n1.terra.api.terra_api.payments.controllers;

import com.ak4n1.terra.api.terra_api.payments.factory.PaymentStrategyFactory;
import com.ak4n1.terra.api.terra_api.payments.strategies.WebhookStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para webhooks - Refactorizado con Factory Pattern
 * CORS manejado por SecurityConfig (no usar @CrossOrigin aquí)
 * Soporta múltiples proveedores de pago (MercadoPago, PayPal, etc.)
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;
    
    /**
     * Webhook de MercadoPago
     */
    @PostMapping(value = "/mercadopago", consumes = {"application/json", "text/plain"})
    public ResponseEntity<Map<String, String>> handleMercadoPagoWebhook(HttpServletRequest request) {
        return handleWebhook(request, "mercadopago");
    }
    
    /**
     * Webhook de PayPal
     */
    @PostMapping(value = "/paypal", consumes = {"application/json", "text/plain"})
    public ResponseEntity<Map<String, String>> handlePayPalWebhook(HttpServletRequest request) {
        return handleWebhook(request, "paypal");
    }
    
    /**
     * Webhook genérico (mantener por retrocompatibilidad con /webhook)
     */
    @PostMapping(value = "/webhook", consumes = {"application/json", "text/plain"})
    public ResponseEntity<Map<String, String>> handleLegacyWebhook(HttpServletRequest request) {
        return handleWebhook(request, "mercadopago"); // Por defecto MercadoPago
    }
    
    /**
     * Método genérico para procesar webhooks de cualquier proveedor
     */
    private ResponseEntity<Map<String, String>> handleWebhook(HttpServletRequest request, String provider) {
        logger.info("\n" +
            "╔══════════════════════════════════════════════════════════════╗\n" +
            "║  🔔 WEBHOOK RECIBIDO - {}                                   ║\n" +
            "╚══════════════════════════════════════════════════════════════╝",
            provider.toUpperCase());
        logger.info("📥 [WEBHOOK-{}] IP: {}, User-Agent: {}", provider, request.getRemoteAddr(), request.getHeader("User-Agent"));
        
        try {
            // Leer el payload
            String payload = readRequestBody(request);
            logger.info("📦 [WEBHOOK-{}] Payload size: {} caracteres", provider, payload.length());
            logger.debug("📦 [WEBHOOK-{}] Full payload: {}", provider, payload);
            
            // Extraer headers
            Map<String, String> headers = extractHeaders(request);
            
            // Obtener la estrategia de webhook correspondiente
            WebhookStrategy strategy = paymentStrategyFactory.getWebhookStrategy(provider);
            
            // Verificar firma del webhook
            boolean verified = strategy.verifyWebhook(headers, payload);
            if (!verified) {
                logger.error("❌ [WEBHOOK-{}] Signature verification FAILED - rejecting webhook", provider);
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Procesar el webhook
            logger.info("⚙️  [WEBHOOK-{}] Procesando webhook...", provider);
            String result = strategy.processWebhook(payload);
            logger.info("✅ [WEBHOOK-{}] Webhook procesado exitosamente: {}", provider, result);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", result);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ [WEBHOOK-{}] Unsupported provider: {}", provider, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unsupported webhook provider");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("❌ [WEBHOOK-{}] Error processing webhook: {}", provider, e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Extraer headers del request y convertirlos a un Map
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName.toLowerCase(), request.getHeader(headerName));
        }
        
        return headers;
    }
    
    /**
     * Endpoint de prueba para webhooks
     */
    @PostMapping("/webhook/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@RequestBody String payload) {
        try {
            logger.info("Webhook de prueba recibido (longitud: {} chars)", payload != null ? payload.length() : 0);
            logger.trace("Payload de prueba (solo TRACE): {}", payload);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook de prueba procesado correctamente");
            // SEGURIDAD: No devolver payload completo en respuesta
            response.put("payloadLength", payload != null ? payload.length() : 0);
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
            
            logger.info("🎭 [SIMULATE] Payload simulado (longitud: {} chars)", simulatedPayload.length());
            logger.trace("🎭 [SIMULATE] Payload simulado completo (solo TRACE): {}", simulatedPayload);
            
            // Procesar el webhook simulado usando la estrategia
            WebhookStrategy strategy = paymentStrategyFactory.getWebhookStrategy("mercadopago");
            String result = strategy.processWebhook(simulatedPayload);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook simulado procesado correctamente");
            response.put("result", result);
            response.put("paymentId", paymentId);
            response.put("preferenceId", preferenceId);
            response.put("webhookStatus", status);
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
