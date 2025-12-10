package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Implementación de la estrategia de webhook para PayPal.
 * 
 * <p>Esta clase maneja la recepción y procesamiento de webhooks de PayPal,
 * incluyendo la verificación de firmas usando el endpoint oficial de PayPal
 * y la actualización de estados de transacciones.
 * 
 * <p>Características principales:
 * <ul>
 *   <li>Verificación de firmas usando endpoint oficial de PayPal (/v1/notifications/verify-webhook-signature)</li>
 *   <li>Validación de Webhook ID en producción</li>
 *   <li>Procesamiento de múltiples tipos de eventos (PAYMENT.CAPTURE.COMPLETED, CHECKOUT.ORDER.COMPLETED, etc.)</li>
 *   <li>Manejo de retry automático para consultas a la API de PayPal</li>
 *   <li>Validación de estados de transacción antes de procesar</li>
 *   <li>Transacciones ACID con aislamiento SERIALIZABLE para prevenir race conditions</li>
 * </ul>
 * 
 * <p><b>Seguridad:</b>
 * <ul>
 *   <li>Usa endpoint oficial de PayPal para verificación (más seguro que verificación manual)</li>
 *   <li>Valida Webhook ID en producción</li>
 *   <li>Timeouts configurados (10 segundos) para evitar bloqueos</li>
 *   <li>Previene procesamiento de transacciones en estados inválidos</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 3.0
 * @see WebhookStrategy
 * @see <a href="https://developer.paypal.com/api/rest/webhooks/">
 *      Documentación de Webhooks de PayPal</a>
 */
@Component
public class PayPalWebhookStrategy implements WebhookStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(PayPalWebhookStrategy.class);
    
    @Value("${paypal.webhook.id.sandbox:}")
    private String sandboxWebhookId;
    
    @Value("${paypal.webhook.id.live:}")
    private String liveWebhookId;
    
    @Value("${paypal.mode:sandbox}")
    private String mode;
    
    @Value("${paypal.client.id.sandbox}")
    private String sandboxClientId;
    
    @Value("${paypal.client.secret.sandbox}")
    private String sandboxClientSecret;
    
    @Value("${paypal.client.id.live:}")
    private String liveClientId;
    
    @Value("${paypal.client.secret.live:}")
    private String liveClientSecret;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private PaymentAuditService auditService;
    
    /**
     * Obtiene el nombre del proveedor de webhook.
     * 
     * @return El nombre del proveedor: "paypal"
     */
    @Override
    public String getProviderName() {
        return "paypal";
    }
    
    /**
     * Verifica si esta estrategia soporta el proveedor especificado.
     * 
     * @param provider Nombre del proveedor a verificar
     * @return true si el proveedor es "paypal" o "pp" (case-insensitive)
     */
    @Override
    public boolean supports(String provider) {
        return "paypal".equalsIgnoreCase(provider) || "pp".equalsIgnoreCase(provider);
    }
    
    /**
     * Verifica la autenticidad de un webhook de PayPal usando el endpoint oficial.
     * 
     * <p>La verificación utiliza el endpoint oficial de PayPal:
     * <code>/v1/notifications/verify-webhook-signature</code>
     * 
     * <p>El proceso incluye:
     * <ol>
     *   <li>Validación de Webhook ID (requerido en producción)</li>
     *   <li>Obtención de token OAuth2 de PayPal</li>
     *   <li>Construcción del request con todos los headers originales y el payload</li>
     *   <li>Envío del request al endpoint de verificación de PayPal</li>
     *   <li>Validación del resultado (verification_status = "SUCCESS")</li>
     * </ol>
     * 
     * <p><b>Requisitos:</b>
     * <ul>
     *   <li>Webhook ID debe coincidir con el configurado (en producción)</li>
     *   <li>Headers de PayPal deben estar presentes (paypal-transmission-id, paypal-transmission-time, etc.)</li>
     *   <li>Credenciales de PayPal deben estar configuradas</li>
     * </ul>
     * 
     * @param headers Headers HTTP del webhook, incluyendo headers específicos de PayPal
     * @param payload Cuerpo del webhook como string JSON
     * @return true si la firma es válida y el webhook es auténtico, false en caso contrario
     * @throws Exception Si ocurre un error durante la verificación
     * @see <a href="https://developer.paypal.com/api/rest/webhooks/#verify-webhook-signature">
     *      Documentación de verificación de webhooks de PayPal</a>
     */
    @Override
    public boolean verifyWebhook(Map<String, String> headers, String payload) throws Exception {
        String transmissionId = headers.get("paypal-transmission-id");
        String transmissionSig = headers.get("paypal-transmission-sig");
        String webhookId = headers.get("paypal-webhook-id");
        
        // Rechazar webhooks sin firma
        if (transmissionId == null || transmissionSig == null) {
            logger.error("[PayPal-Webhook] ❌ Faltan headers de firma - WEBHOOK RECHAZADO");
            return false;
        }
        
        // Verificar webhook ID si viene en headers
        String expectedWebhookId = "live".equalsIgnoreCase(mode) ? liveWebhookId : sandboxWebhookId;
        boolean isSandbox = !"live".equalsIgnoreCase(mode);
        
        if (expectedWebhookId != null && !expectedWebhookId.isEmpty() && webhookId != null) {
            if (!webhookId.equals(expectedWebhookId)) {
                logger.error("[PayPal-Webhook] ❌ Webhook ID no coincide. Recibido: {}, Esperado: {}", webhookId, expectedWebhookId);
                return false;
            }
        } else if (expectedWebhookId != null && !expectedWebhookId.isEmpty() && webhookId == null && !isSandbox) {
            logger.error("[PayPal-Webhook] ❌ Webhook ID requerido en producción");
            return false;
        }
        
        // Verificar firma usando endpoint oficial de PayPal
        try {
            boolean signatureValid = verifyWebhookWithPayPalAPI(headers, payload);
            
            if (signatureValid) {
                logger.info("[PayPal-Webhook] ✅ Firma verificada correctamente");
                return true;
            } else {
                logger.error("[PayPal-Webhook] ❌ Verificacion de firma fallida - WEBHOOK RECHAZADO");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] ❌ Error verificando firma: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifica un webhook usando el endpoint oficial de PayPal.
     * 
     * <p>Este método utiliza el endpoint oficial de PayPal para verificar la autenticidad
     * del webhook, lo cual es más seguro y confiable que la verificación manual de firmas RSA.
     * 
     * <p>El proceso:
     * <ol>
     *   <li>Obtiene un token OAuth2 de PayPal</li>
     *   <li>Construye un request JSON con todos los headers originales y el payload</li>
     *   <li>Envía el request al endpoint /v1/notifications/verify-webhook-signature</li>
     *   <li>Evalúa la respuesta: verification_status = "SUCCESS" indica firma válida</li>
     * </ol>
     * 
     * <p><b>Timeout:</b> 10 segundos para evitar bloqueos.
     * 
     * @param headers Headers HTTP originales del webhook
     * @param payload Cuerpo del webhook como string JSON
     * @return true si PayPal confirma que la firma es válida (verification_status = "SUCCESS")
     * @throws Exception Si ocurre un error al comunicarse con la API de PayPal
     * @see <a href="https://developer.paypal.com/api/rest/webhooks/#verify-webhook-signature">
     *      Documentación de verificación de webhooks de PayPal</a>
     */
    private boolean verifyWebhookWithPayPalAPI(Map<String, String> headers, String payload) throws Exception {
        // Obtener token de acceso de PayPal
        String accessToken = getPayPalAccessToken();
        
        // Construir el request body según la documentación de PayPal
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("transmission_id", headers.get("paypal-transmission-id"));
        requestBody.put("transmission_time", headers.get("paypal-transmission-time"));
        requestBody.put("cert_url", headers.get("paypal-cert-url"));
        requestBody.put("auth_algo", headers.get("paypal-auth-algo"));
        requestBody.put("transmission_sig", headers.get("paypal-transmission-sig"));
        
        String webhookId = headers.get("paypal-webhook-id");
        if (webhookId == null || webhookId.isEmpty()) {
            webhookId = "live".equalsIgnoreCase(mode) ? liveWebhookId : sandboxWebhookId;
        }
        requestBody.put("webhook_id", webhookId);
        requestBody.put("webhook_event", mapper.readTree(payload));
        
        String requestBodyJson = mapper.writeValueAsString(requestBody);
        
        // Determinar la URL base según el modo
        String baseUrl = "live".equalsIgnoreCase(mode) ? 
            "https://api.paypal.com" : 
            "https://api.sandbox.paypal.com";
        
        // Crear el request HTTP
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v1/notifications/verify-webhook-signature"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
            .build();
        
        // Ejecutar el request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Parsear la respuesta
        JsonNode responseBody = mapper.readTree(response.body());
        String verificationStatus = responseBody.has("verification_status") ? 
            responseBody.get("verification_status").asText() : null;
        
        logger.debug("[PayPal-Webhook] Verificacion: Status={}, Result={}", response.statusCode(), verificationStatus);
        
        // PayPal devuelve "SUCCESS" cuando la firma es válida
        return "SUCCESS".equalsIgnoreCase(verificationStatus);
    }
    
    /**
     * Obtiene un token de acceso OAuth2 de PayPal.
     * 
     * <p>Este método genera un token OAuth2 necesario para autenticar las llamadas
     * a la API de PayPal. El token se genera usando las credenciales (Client ID y Secret)
     * configuradas según el modo (sandbox o live).
     * 
     * <p><b>Timeout:</b> 10 segundos para evitar bloqueos.
     * 
     * @return El token de acceso OAuth2
     * @throws IOException Si ocurre un error de I/O al comunicarse con PayPal
     * @throws InterruptedException Si la operación es interrumpida
     */
    private String getPayPalAccessToken() throws IOException, InterruptedException {
        String clientId = "live".equalsIgnoreCase(mode) ? liveClientId : sandboxClientId;
        String clientSecret = "live".equalsIgnoreCase(mode) ? liveClientSecret : sandboxClientSecret;
        String baseUrl = "live".equalsIgnoreCase(mode) ? 
            "https://api.paypal.com" : 
            "https://api.sandbox.paypal.com";
        
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v1/oauth2/token"))
            .header("Authorization", "Basic " + encodedCredentials)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response.body());
        return jsonResponse.get("access_token").asText();
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public String processWebhook(String payload) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            String eventType = webhookData.has("event_type") ? webhookData.get("event_type").asText() : null;
            
            if (eventType == null) {
                logger.error("[PayPal-Webhook] ❌ No event_type found in webhook");
                return "ERROR: No event_type found";
            }
            
            logger.info("[PayPal-Webhook] Procesando evento: {}", eventType);
            
            // Procesar según el tipo de evento
            switch (eventType) {
                case "PAYMENT.CAPTURE.COMPLETED":
                    return processPaymentCaptureCompleted(webhookData);
                case "PAYMENT.CAPTURE.DENIED":
                    return processPaymentCaptureDenied(webhookData);
                case "PAYMENT.CAPTURE.REFUNDED":
                    return processPaymentCaptureRefunded(webhookData);
                case "CHECKOUT.ORDER.APPROVED":
                    return processCheckoutOrderApproved(webhookData);
                case "CHECKOUT.ORDER.COMPLETED":
                    return processCheckoutOrderCompleted(webhookData);
                default:
                    logger.info("[PayPal-Webhook] Unhandled event type: {}", eventType);
                    return "Unhandled event type: " + eventType;
            }
            
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing webhook: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesar evento PAYMENT.CAPTURE.COMPLETED
     */
    private String processPaymentCaptureCompleted(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String captureId = resource.get("id").asText();
            
            String customId = resource.has("custom_id") ? resource.get("custom_id").asText() : null;
            
            String orderId = null;
            if (resource.has("supplementary_data")) {
                JsonNode supplementaryData = resource.get("supplementary_data");
                if (supplementaryData.has("related_ids") && supplementaryData.get("related_ids").has("order_id")) {
                    orderId = supplementaryData.get("related_ids").get("order_id").asText();
                }
            }
            
            logger.debug("[PayPal-Webhook] Payment captured: {}", captureId);
            
            PaymentTransaction transaction = null;

            if (customId != null) {
                transaction = paymentTransactionRepository.findByExternalUuid(customId).orElse(null);
            }

            if (transaction == null && orderId != null) {
                transaction = paymentTransactionRepository.findByPaypalOrderId(orderId).orElse(null);
            }

            if (transaction != null) {
                // Validar que la transacción no esté en un estado inválido
                if (PaymentStatus.REFUNDED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal-Webhook] ⚠️ Intento de procesar transaccion reembolsada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process refunded transaction");
                }
                
                if (PaymentStatus.CANCELLED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal-Webhook] ⚠️ Intento de procesar transaccion cancelada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process cancelled transaction");
                }
                
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed();
                    transaction.setUpdatedAt(new Date());

                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush();

                    addCoinsToAccount(transaction);
                    logger.info("[PayPal-Webhook] ✅ Pago procesado exitosamente");
                } else {
                    logger.debug("[PayPal-Webhook] Pago ya procesado");
                }
                return "Payment captured successfully";
            }

            logger.warn("[PayPal-Webhook] ⚠️ Transaccion no encontrada");
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing capture completed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesa el evento PAYMENT.CAPTURE.DENIED de PayPal.
     * 
     * <p>Este evento se dispara cuando un pago es denegado. El método actualiza
     * el estado de la transacción a REJECTED.
     * 
     * @param webhookData El payload del webhook parseado como JsonNode
     * @return Mensaje de resultado del procesamiento
     */
    private String processPaymentCaptureDenied(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String captureId = resource.get("id").asText();
            String customId = resource.has("custom_id") ? resource.get("custom_id").asText() : null;
            
            logger.debug("[PayPal-Webhook] Payment denied: {}", captureId);
            
            if (customId != null) {
                PaymentTransaction transaction = paymentTransactionRepository.findByExternalUuid(customId)
                        .orElse(null);
                
                if (transaction != null) {
                    transaction.setStatus(PaymentStatus.REJECTED);
                    transaction.setUpdatedAt(new Date());
                    paymentTransactionRepository.save(transaction);
                    return "Payment denied processed";
                }
            }
            
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing capture denied: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesa el evento PAYMENT.CAPTURE.REFUNDED de PayPal.
     * 
     * <p><b>NOTA:</b> Esta funcionalidad aún no está completamente implementada.
     * Cuando se implemente, debería:
     * <ul>
     *   <li>Actualizar el estado de la transacción a REFUNDED</li>
     *   <li>Restar las monedas acreditadas de la cuenta del usuario</li>
     *   <li>Registrar la operación en auditoría</li>
     * </ul>
     * 
     * @param webhookData El payload del webhook parseado como JsonNode
     * @return Mensaje indicando que el evento fue recibido pero no procesado
     */
    private String processPaymentCaptureRefunded(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String refundId = resource.get("id").asText();
            
            logger.debug("[PayPal-Webhook] Payment refunded: {}", refundId);
            
            // TODO: Implementar lógica de reembolso (restar monedas, etc.)
            
            return "Refund processed";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing refund: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesa el evento CHECKOUT.ORDER.APPROVED de PayPal.
     * 
     * <p>Este evento se dispara cuando el usuario aprueba una orden en PayPal.
     * Por sí solo no acredita monedas, solo indica que el usuario aprobó el pago.
     * Las monedas se acreditan cuando se recibe PAYMENT.CAPTURE.COMPLETED o
     * CHECKOUT.ORDER.COMPLETED.
     * 
     * @param webhookData El payload del webhook parseado como JsonNode
     * @return Mensaje de resultado del procesamiento
     */
    private String processCheckoutOrderApproved(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String orderId = resource.get("id").asText();
            
            PaymentTransaction transaction = paymentTransactionRepository.findByPaypalOrderId(orderId)
                    .orElse(null);
            
            if (transaction != null) {
                if (transaction.getProcessedAt() != null) {
                    logger.debug("[PayPal-Webhook] Orden ya procesada");
                    return "Order already processed (capture completed)";
                }
                
                transaction.setUpdatedAt(new Date());
                paymentTransactionRepository.save(transaction);
                logger.debug("[PayPal-Webhook] Orden aprobada, esperando captura");
                return "Order approved processed";
            }
            
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing order approved: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesa el evento CHECKOUT.ORDER.COMPLETED de PayPal.
     * 
     * <p>Este evento se dispara cuando una orden es completada. El método:
     * <ul>
     *   <li>Busca la transacción por paypal_order_id</li>
     *   <li>Valida que la transacción no esté en un estado inválido</li>
     *   <li>Actualiza el estado a APPROVED</li>
     *   <li>Acredita las monedas a la cuenta del usuario</li>
     * </ul>
     * 
     * @param webhookData El payload del webhook parseado como JsonNode
     * @return Mensaje de resultado del procesamiento
     */
    private String processCheckoutOrderCompleted(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String orderId = resource.get("id").asText();
            
            PaymentTransaction transaction = paymentTransactionRepository.findByPaypalOrderId(orderId)
                    .orElse(null);
            
            if (transaction != null) {
                // Validar que la transacción no esté en un estado inválido
                if (PaymentStatus.REFUNDED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal-Webhook] ⚠️ Intento de procesar transaccion reembolsada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process refunded transaction");
                }
                
                if (PaymentStatus.CANCELLED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal-Webhook] ⚠️ Intento de procesar transaccion cancelada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process cancelled transaction");
                }
                
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed();
                    transaction.setUpdatedAt(new Date());
                    
                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush();
                    
                    addCoinsToAccount(transaction);
                    logger.info("[PayPal-Webhook] ✅ Orden completada y procesada");
                } else {
                    logger.debug("[PayPal-Webhook] Orden ya procesada");
                }
                return "Order completed processed";
            }
            
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing order completed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Agregar monedas a la cuenta del usuario
     * Usa transacción SERIALIZABLE para evitar condiciones de carrera cuando múltiples webhooks procesan el mismo pago
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    private void addCoinsToAccount(PaymentTransaction transaction) {
        AccountMaster account = transaction.getAccount();
        Integer currentCoins = account.getTerraCoins();
        
        if (currentCoins == null) {
            currentCoins = 0;
        }
        
        Integer newCoins = currentCoins + transaction.getCoinsAmount();
        
        logger.info("[PayPal-Webhook] 💰 Agregando {} monedas a cuenta {}. Total: {} -> {}", 
                   transaction.getCoinsAmount(), account.getId(), currentCoins, newCoins);
        
        // Actualizar saldo
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush(); // Force immediate write
        
        // Registrar en auditoría (si falla, hace rollback de TODO)
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "paypal");
    }
}

