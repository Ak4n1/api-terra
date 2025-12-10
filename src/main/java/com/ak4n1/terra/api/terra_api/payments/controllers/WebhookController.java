package com.ak4n1.terra.api.terra_api.payments.controllers;

import com.ak4n1.terra.api.terra_api.payments.factory.PaymentStrategyFactory;
import com.ak4n1.terra.api.terra_api.payments.strategies.WebhookStrategy;
import jakarta.servlet.http.HttpServletRequest;
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
        try {
            // Leer el payload
            String payload = readRequestBody(request);
            
            // Extraer headers
            Map<String, String> headers = extractHeaders(request);
            
            // Obtener la estrategia de webhook correspondiente
            WebhookStrategy strategy = paymentStrategyFactory.getWebhookStrategy(provider);
            
            // Verificar firma del webhook
            boolean verified = strategy.verifyWebhook(headers, payload);
            if (!verified) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Procesar el webhook
            String result = strategy.processWebhook(payload);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", result);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unsupported webhook provider");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Extraer headers del request y convertirlos a un Map
     * También incluye query parameters como headers custom para Mercado Pago
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName.toLowerCase(), request.getHeader(headerName));
        }
        
        // Agregar query parameters como headers custom para Mercado Pago
        // Mercado Pago envía el ID como query parameter en diferentes formatos:
        // - ?id=137154843764&topic=payment
        // - ?data.id=137154843764&type=payment
        String queryId = request.getParameter("id");
        if (queryId != null && !queryId.isEmpty()) {
            headers.put("x-query-id", queryId);
        }
        
        // También buscar data.id (formato alternativo de Mercado Pago)
        String queryDataId = request.getParameter("data.id");
        if (queryDataId != null && !queryDataId.isEmpty()) {
            headers.put("x-query-data-id", queryDataId);
        }
        
        String queryTopic = request.getParameter("topic");
        if (queryTopic != null && !queryTopic.isEmpty()) {
            headers.put("x-query-topic", queryTopic);
        }
        
        String queryType = request.getParameter("type");
        if (queryType != null && !queryType.isEmpty()) {
            headers.put("x-query-type", queryType);
        }
        
        return headers;
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
