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

import java.util.Date;
import java.util.Map;

/**
 * PayPal webhook strategy implementation
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
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private PaymentAuditService auditService;
    
    @Override
    public String getProviderName() {
        return "paypal";
    }
    
    @Override
    public boolean supports(String provider) {
        return "paypal".equalsIgnoreCase(provider) || "pp".equalsIgnoreCase(provider);
    }
    
    @Override
    public boolean verifyWebhook(Map<String, String> headers, String payload) throws Exception {
        String environment = "live".equalsIgnoreCase(mode) ? "PRODUCCIÓN (LIVE)" : "DESARROLLO (SANDBOX)";
        logger.info("\n" +
            "╔══════════════════════════════════════════════════════════════╗\n" +
            "║  🔐 PAYPAL WEBHOOK - VERIFICACIÓN DE FIRMA                  ║\n" +
            "║  Entorno: {}                                    ║\n" +
            "╚══════════════════════════════════════════════════════════════╝",
            environment);
        
        // PayPal envía varios headers para la verificación
        String transmissionId = headers.get("paypal-transmission-id");
        String transmissionSig = headers.get("paypal-transmission-sig");
        String authAlgo = headers.get("paypal-auth-algo");
        String certUrl = headers.get("paypal-cert-url");
        String transmissionTime = headers.get("paypal-transmission-time");
        String webhookId = headers.get("paypal-webhook-id");
        
        logger.info("\n" +
            "📋 ──────────────────────────────────────────────────────────\n" +
            "📋 HEADERS RECIBIDOS DE PAYPAL:\n" +
            "📋 ──────────────────────────────────────────────────────────\n" +
            "📋 paypal-transmission-id: {}\n" +
            "📋 paypal-transmission-sig: {}\n" +
            "📋 paypal-auth-algo: {}\n" +
            "📋 paypal-cert-url: {}\n" +
            "📋 paypal-transmission-time: {}\n" +
            "📋 paypal-webhook-id: {}\n" +
            "📋 Payload Length: {} caracteres\n" +
            "📋 ──────────────────────────────────────────────────────────",
            transmissionId != null ? transmissionId.substring(0, Math.min(50, transmissionId.length())) + "..." : "null",
            transmissionSig != null ? transmissionSig.substring(0, Math.min(50, transmissionSig.length())) + "..." : "null",
            authAlgo != null ? authAlgo : "null",
            certUrl != null ? (certUrl.length() > 80 ? certUrl.substring(0, 80) + "..." : certUrl) : "null",
            transmissionTime != null ? transmissionTime : "null",
            webhookId != null ? webhookId : "null",
            payload != null ? payload.length() : 0);
        
        // Rechazar webhooks sin firma (seguridad en producción)
        if (transmissionId == null || transmissionSig == null) {
            logger.error("\n" +
                "❌ ═══════════════════════════════════════════════════════════\n" +
                "❌ NO SE PROPORCIONARON HEADERS DE FIRMA\n" +
                "❌ ═══════════════════════════════════════════════════════════\n" +
                "❌ Faltan: paypal-transmission-id o paypal-transmission-sig\n" +
                "❌ WEBHOOK RECHAZADO - Firma requerida para seguridad\n" +
                "❌ ═══════════════════════════════════════════════════════════\n");
            return false;
        }
        
        // Verificar que el webhook ID coincida (si está configurado)
        String expectedWebhookId = "live".equalsIgnoreCase(mode) ? liveWebhookId : sandboxWebhookId;
        boolean isSandbox = !"live".equalsIgnoreCase(mode);
        
        if (expectedWebhookId != null && !expectedWebhookId.isEmpty()) {
            // Si el webhook ID viene en headers, verificarlo estrictamente
            if (webhookId != null) {
                if (!webhookId.equals(expectedWebhookId)) {
                    logger.error("\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n" +
                        "❌ WEBHOOK ID NO COINCIDE\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n" +
                        "❌ Webhook ID recibido: {}\n" +
                        "❌ Webhook ID esperado: {}\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n",
                        webhookId,
                        expectedWebhookId);
                    return false;
                }
                logger.info("\n" +
                    "✅ ──────────────────────────────────────────────────────────\n" +
                    "✅ WEBHOOK ID VERIFICADO\n" +
                    "✅ ──────────────────────────────────────────────────────────\n" +
                    "✅ Webhook ID: {}\n" +
                    "✅ ──────────────────────────────────────────────────────────",
                    webhookId);
            } else {
                // El webhook ID no viene en headers (puede ser herramienta de test)
                if (isSandbox) {
                    // En sandbox, permitir si tiene los headers de firma (para testing)
                    logger.warn("\n" +
                        "⚠️  ═══════════════════════════════════════════════════════════\n" +
                        "⚠️  WEBHOOK ID NO ENCONTRADO EN HEADERS\n" +
                        "⚠️  ═══════════════════════════════════════════════════════════\n" +
                        "⚠️  Esto puede ocurrir con la herramienta de test de PayPal\n" +
                        "⚠️  Permitiendo en modo SANDBOX porque tiene headers de firma\n" +
                        "⚠️  Webhook ID esperado: {}\n" +
                        "⚠️  ═══════════════════════════════════════════════════════════\n",
                        expectedWebhookId);
                } else {
                    // En producción (live), siempre requerir el webhook ID
                    logger.error("\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n" +
                        "❌ WEBHOOK ID REQUERIDO EN PRODUCCIÓN\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n" +
                        "❌ El header 'paypal-webhook-id' es obligatorio en modo LIVE\n" +
                        "❌ Webhook ID esperado: {}\n" +
                        "❌ ═══════════════════════════════════════════════════════════\n",
                        expectedWebhookId);
                    return false;
                }
            }
        } else {
            logger.warn("\n" +
                "⚠️  ═══════════════════════════════════════════════════════════\n" +
                "⚠️  WEBHOOK ID NO CONFIGURADO\n" +
                "⚠️  ═══════════════════════════════════════════════════════════\n" +
                "⚠️  Configure paypal.webhook.id.sandbox o paypal.webhook.id.live\n" +
                "⚠️  ═══════════════════════════════════════════════════════════\n");
        }
        
        // TODO: Implementar verificación completa de firma de PayPal
        // PayPal requiere:
        // 1. Descargar certificado desde certUrl
        // 2. Construir cadena: transmissionId|transmissionTime|webhookId|crc32(payload)
        // 3. Verificar firma usando el certificado y authAlgo (normalmente SHA256withRSA)
        // Ver: https://developer.paypal.com/api/rest/webhooks/#verify-signature
        
        // Si llegamos aquí, las verificaciones básicas pasaron
        logger.info("\n" +
            "✅ ═══════════════════════════════════════════════════════════\n" +
            "✅ VERIFICACIÓN BÁSICA EXITOSA - WEBHOOK ACEPTADO\n" +
            "✅ ═══════════════════════════════════════════════════════════\n" +
            "✅ Entorno: {}\n" +
            "✅ Headers de firma presentes\n" +
            "✅ Webhook ID verificado (si aplica)\n" +
            "⚠️  NOTA: Verificación completa de firma con certificado pendiente\n" +
            "✅ ═══════════════════════════════════════════════════════════",
            environment);
        
        logger.warn("\n" +
            "⚠️  ═══════════════════════════════════════════════════════════\n" +
            "⚠️  VERIFICACIÓN DE FIRMA NO IMPLEMENTADA COMPLETAMENTE\n" +
            "⚠️  ═══════════════════════════════════════════════════════════\n" +
            "⚠️  Solo se verifica el Webhook ID y headers básicos\n" +
            "⚠️  La verificación de firma con certificado está pendiente\n" +
            "⚠️  ═══════════════════════════════════════════════════════════\n");
        
        return true;
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public String processWebhook(String payload) throws Exception {
        logger.info("[PayPal-Webhook] Processing webhook");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            // Extraer información del webhook
            String apiVersion = webhookData.has("api_version") ? webhookData.get("api_version").asText() : null;
            String eventType = webhookData.has("event_type") ? webhookData.get("event_type").asText() : null;
            String webhookId = webhookData.has("id") ? webhookData.get("id").asText() : null;
            
            logger.info("\n" +
                "📦 ──────────────────────────────────────────────────────────\n" +
                "📦 PAYLOAD DEL WEBHOOK:\n" +
                "📦 ──────────────────────────────────────────────────────────\n" +
                "📦 API Version: {}\n" +
                "📦 Event Type: {}\n" +
                "📦 Webhook ID: {}\n" +
                "📦 Payload completo (primeros 200 chars):\n" +
                "📦 {}\n" +
                "📦 ──────────────────────────────────────────────────────────",
                apiVersion != null ? apiVersion : "no especificado",
                eventType != null ? eventType : "no encontrado",
                webhookId != null ? webhookId : "no encontrado",
                payload.length() > 200 ? payload.substring(0, 200) + "..." : payload);
            
            if (eventType == null) {
                logger.error("[PayPal-Webhook] ❌ No event_type found in webhook");
                return "ERROR: No event_type found";
            }
            
            logger.info("[PayPal-Webhook] ✅ Event type: {}", eventType);
            
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
            
            // Extraer custom_id que contiene nuestro transaction ID
            String customId = resource.has("custom_id") ? resource.get("custom_id").asText() : null;
            
            // Extraer order_id como fallback
            String orderId = null;
            if (resource.has("supplementary_data")) {
                JsonNode supplementaryData = resource.get("supplementary_data");
                if (supplementaryData.has("related_ids") && supplementaryData.get("related_ids").has("order_id")) {
                    orderId = supplementaryData.get("related_ids").get("order_id").asText();
                }
            }
            
            logger.info("[PayPal-Webhook] Payment captured: {}, customId (UUID): {}, orderId: {}", captureId, customId, orderId);
            
            PaymentTransaction transaction = null;
            
            // Intentar buscar por UUID primero
            if (customId != null) {
                transaction = paymentTransactionRepository.findByExternalUuid(customId).orElse(null);
                if (transaction != null) {
                    logger.info("[PayPal-Webhook] Transaction found by UUID: {}", transaction.getId());
                }
            }
            
            // Si no se encontró por UUID, intentar por order_id
            if (transaction == null && orderId != null) {
                transaction = paymentTransactionRepository.findByPaypalOrderId(orderId).orElse(null);
                if (transaction != null) {
                    logger.info("[PayPal-Webhook] Transaction found by orderId: {}", transaction.getId());
                }
            }
            
            if (transaction != null) {
                // VERIFICACIÓN DOBLE: Estado Y processedAt
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    logger.info("[PayPal-Webhook] Processing payment for the first time (webhook)");
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed(); // Marcar como procesado
                    transaction.setUpdatedAt(new Date());
                    
                    // Guardar ANTES de agregar monedas
                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush();
                    
                    // Agregar monedas
                    addCoinsToAccount(transaction);
                } else {
                    logger.warn("[PayPal-Webhook] Payment already processed! Status: {}, ProcessedAt: {}", 
                               transaction.getStatus(), transaction.getProcessedAt());
                }
                return "Payment captured successfully";
            }
            
            logger.warn("[PayPal-Webhook] Transaction not found. UUID: {}, OrderId: {}", customId, orderId);
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing capture completed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesar evento PAYMENT.CAPTURE.DENIED
     */
    private String processPaymentCaptureDenied(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String captureId = resource.get("id").asText();
            String customId = resource.has("custom_id") ? resource.get("custom_id").asText() : null;
            
            logger.info("[PayPal-Webhook] Payment denied: {}, customId (UUID): {}", captureId, customId);
            
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
     * Procesar evento PAYMENT.CAPTURE.REFUNDED
     */
    private String processPaymentCaptureRefunded(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String refundId = resource.get("id").asText();
            
            logger.info("[PayPal-Webhook] Payment refunded: {}", refundId);
            
            // TODO: Implementar lógica de reembolso (restar monedas, etc.)
            
            return "Refund processed";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing refund: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesar evento CHECKOUT.ORDER.APPROVED
     */
    private String processCheckoutOrderApproved(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String orderId = resource.get("id").asText();
            
            logger.info("[PayPal-Webhook] Order approved: {}", orderId);
            
            PaymentTransaction transaction = paymentTransactionRepository.findByPaypalOrderId(orderId)
                    .orElse(null);
            
            if (transaction != null) {
                // NO CAMBIAR el estado si ya fue capturado/procesado
                if (transaction.getProcessedAt() != null) {
                    logger.info("[PayPal-Webhook] Order already processed (capture finished). Status: {}", transaction.getStatus());
                    return "Order already processed (capture completed)";
                }
                
                // Si aún no fue procesado, solo actualizar timestamp (no cambiar estado)
                logger.info("[PayPal-Webhook] Order approved but not captured yet. Keeping status: {}", transaction.getStatus());
                transaction.setUpdatedAt(new Date());
                paymentTransactionRepository.save(transaction);
                return "Order approved processed";
            }
            
            return "Transaction not found";
        } catch (Exception e) {
            logger.error("[PayPal-Webhook] Error processing order approved: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Procesar evento CHECKOUT.ORDER.COMPLETED
     */
    private String processCheckoutOrderCompleted(JsonNode webhookData) {
        try {
            JsonNode resource = webhookData.get("resource");
            String orderId = resource.get("id").asText();
            
            logger.info("[PayPal-Webhook] Order completed: {}", orderId);
            
            PaymentTransaction transaction = paymentTransactionRepository.findByPaypalOrderId(orderId)
                    .orElse(null);
            
            if (transaction != null) {
                // VERIFICACIÓN DOBLE: Estado Y processedAt
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    logger.info("[PayPal-Webhook] Processing order completion for the first time");
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed();
                    transaction.setUpdatedAt(new Date());
                    
                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush();
                    
                    addCoinsToAccount(transaction);
                } else {
                    logger.warn("[PayPal-Webhook] Order already processed! Status: {}, ProcessedAt: {}", 
                               transaction.getStatus(), transaction.getProcessedAt());
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
     * CRITICAL: Must be called within a SERIALIZABLE transaction
     */
    private void addCoinsToAccount(PaymentTransaction transaction) {
        AccountMaster account = transaction.getAccount();
        Integer currentCoins = account.getTerraCoins();
        
        if (currentCoins == null) {
            currentCoins = 0;
        }
        
        Integer newCoins = currentCoins + transaction.getCoinsAmount();
        
        logger.info("[PayPal-Webhook] Adding {} coins to account {}. Total: {} -> {}", 
                   transaction.getCoinsAmount(), account.getId(), currentCoins, newCoins);
        
        // Actualizar saldo
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush(); // Force immediate write
        
        // Registrar en auditoría (si falla, hace rollback de TODO)
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "paypal");
    }
}

