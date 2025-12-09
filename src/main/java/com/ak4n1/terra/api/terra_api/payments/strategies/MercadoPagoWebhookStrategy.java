package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * MercadoPago webhook strategy implementation
 */
@Component
public class MercadoPagoWebhookStrategy implements WebhookStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookStrategy.class);
    
    @Value("${mercadopago.access.token}")
    private String accessToken;
    
    @Value("${mercadopago.webhook.secret:}")
    private String webhookSecret;
    
    @Value("${mercadopago.notification.url:}")
    private String notificationUrl;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private PaymentAuditService auditService;
    
    @Override
    public String getProviderName() {
        return "mercadopago";
    }
    
    @Override
    public boolean supports(String provider) {
        return "mercadopago".equalsIgnoreCase(provider) || "mp".equalsIgnoreCase(provider);
    }
    
    @Override
    public boolean verifyWebhook(Map<String, String> headers, String payload) throws Exception {
        // Determinar entorno basado en la URL de notificación
        String environment = (notificationUrl != null && (notificationUrl.contains("ngrok") || notificationUrl.contains("localhost"))) 
            ? "DESARROLLO" : "PRODUCCIÓN";
        
        logger.info("\n" +
            "╔══════════════════════════════════════════════════════════════╗\n" +
            "║  🔐 MERCADO PAGO WEBHOOK - VERIFICACIÓN DE FIRMA           ║\n" +
            "║  Entorno: {}                                    ║\n" +
            "╚══════════════════════════════════════════════════════════════╝",
            environment);
        
        String signature = headers.get("x-signature");
        
        // Rechazar webhooks sin firma (seguridad en producción)
        if (signature == null || signature.isEmpty()) {
            logger.error("[MP-Webhook] ❌ No se proporcionó firma - WEBHOOK RECHAZADO");
            return false;
        }
        
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            logger.error("[MP-Webhook] ❌ No hay webhook secret configurado");
            return false;
        }
        
        try {
            // Mercado Pago envía la firma en formato: "ts=<timestamp>,v1=<hash>"
            String[] signatureParts = extractSignatureParts(signature);
            if (signatureParts == null) {
                logger.error("[MP-Webhook] ❌ Formato de firma inválido: {}", signature);
                return false;
            }
            
            String timestamp = signatureParts[0];
            String receivedHash = signatureParts[1];
            
            // Según documentación oficial de Mercado Pago:
            // La cadena de datos debe ser: id:[data.id];request-id:[x-request-id];ts:[ts];
            String requestId = headers.get("x-request-id");
            if (requestId == null || requestId.isEmpty()) {
                logger.error("[MP-Webhook] ❌ Falta header x-request-id");
                return false;
            }
            
            // Extraer el ID de la notificación del payload
            String notificationId = extractNotificationId(payload);
            if (notificationId == null) {
                logger.error("[MP-Webhook] ❌ No se pudo extraer ID de la notificación del payload");
                return false;
            }
            
            // Construir la cadena de datos según documentación de Mercado Pago
            String dataToSign = String.format("id:%s;request-id:%s;ts:%s;", 
                notificationId.toLowerCase(), requestId, timestamp);
            
            String calculatedHash = calculateHMAC(dataToSign, webhookSecret);
            
            // Comparar de forma segura (evita timing attacks)
            boolean isValid = MessageDigest.isEqual(
                receivedHash.getBytes(StandardCharsets.UTF_8),
                calculatedHash.getBytes(StandardCharsets.UTF_8)
            );
            
            if (isValid) {
                logger.info("\n" +
                    "✅ ═══════════════════════════════════════════════════════════\n" +
                    "✅ VERIFICACIÓN EXITOSA - WEBHOOK VÁLIDO\n" +
                    "✅ ═══════════════════════════════════════════════════════════\n" +
                    "✅ Entorno: {}\n" +
                    "✅ Firma HMAC-SHA256 verificada correctamente\n" +
                    "✅ ═══════════════════════════════════════════════════════════",
                    environment);
            } else {
                logger.error("\n" +
                    "❌ ═══════════════════════════════════════════════════════════\n" +
                    "❌ VERIFICACIÓN FALLIDA - WEBHOOK RECHAZADO\n" +
                    "❌ ═══════════════════════════════════════════════════════════\n" +
                    "❌ Entorno: {}\n" +
                    "❌ Hash recibido: {}...\n" +
                    "❌ Hash calculado: {}...\n" +
                    "❌ Las firmas NO coinciden\n" +
                    "❌ ═══════════════════════════════════════════════════════════",
                    environment,
                    receivedHash.length() > 20 ? receivedHash.substring(0, 20) : receivedHash,
                    calculatedHash.length() > 20 ? calculatedHash.substring(0, 20) : calculatedHash);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] ❌ Error verificando firma: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Extraer el ID de la notificación del payload
     * Puede estar en: data.id, id, o data.id (según formato)
     */
    private String extractNotificationId(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            // Formato 1: {"data":{"id":"123456"}}
            if (webhookData.has("data") && webhookData.get("data").has("id")) {
                return webhookData.get("data").get("id").asText();
            }
            
            // Formato 2: {"id":"123456"}
            if (webhookData.has("id")) {
                return webhookData.get("id").asText();
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error extrayendo ID de notificación: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extraer los componentes de la firma de Mercado Pago
     * Formato esperado: "ts=<timestamp>,v1=<hash>"
     * @return Array con [timestamp, hash] o null si el formato es inválido
     */
    private String[] extractSignatureParts(String signature) {
        if (signature == null || signature.isEmpty()) {
            return null;
        }
        
        try {
            // Formato: ts=1765289902349,v1=fcaf4765432ab96bfa0fe12cad33844cfb0724d0eb1e67b5faa80da99a1534f6
            String[] parts = signature.split(",");
            if (parts.length != 2) {
                logger.warn("⚠️ Formato de firma inesperado. Partes encontradas: {}", parts.length);
                return null;
            }
            
            String timestamp = null;
            String hash = null;
            
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("ts=")) {
                    timestamp = part.substring(3);
                } else if (part.startsWith("v1=")) {
                    hash = part.substring(3);
                }
            }
            
            if (timestamp == null || hash == null) {
                logger.warn("⚠️ No se encontraron ts o v1 en la firma");
                return null;
            }
            
            return new String[]{timestamp, hash};
            
        } catch (Exception e) {
            logger.error("❌ Error extrayendo componentes de la firma: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Calcular HMAC-SHA256 del payload usando el webhook secret
     * Según documentación de Mercado Pago:
     * https://www.mercadopago.com.ar/developers/es/guides/notifications/webhooks#bookmark_validar_la_autenticidad_de_una_notificaci%C3%B3n
     */
    private String calculateHMAC(String payload, String secret) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal (lowercase, sin separadores)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] Error calculando HMAC: {}", e.getMessage(), e);
            throw new Exception("Failed to calculate HMAC: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public String processWebhook(String payload) throws Exception {
        logger.info("[MP-Webhook] Processing webhook");
        
        try {
            // Parsear el payload del webhook
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            // Extraer el ID del pago - MÚLTIPLES FORMATOS DE MERCADO PAGO
            String paymentId = extractPaymentId(webhookData);
            
            if (paymentId == null) {
                logger.error("[MP-Webhook] No se pudo extraer el ID del pago del webhook");
                logger.trace("[MP-Webhook] Payload structure: {}", webhookData.toString());
                return "ERROR: No payment ID found";
            }
            
            // Verificar si es un webhook de prueba
            if ("123456".equals(paymentId)) {
                logger.info("[MP-Webhook] Test webhook received, ignoring");
                return "Test webhook processed";
            }
            
            // Obtener información del pago desde Mercado Pago
            Payment payment = getPaymentFromMercadoPago(paymentId);
            if (payment == null) {
                logger.warn("[MP-Webhook] Could not get payment from MercadoPago: {}", paymentId);
                return "Payment not found in MercadoPago";
            }
            
            // Procesar el pago según su estado
            processPaymentStatus(payment);
            
            return "Webhook processed successfully";
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] Error processing webhook: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Extraer payment ID del webhook según los diferentes formatos de MercadoPago
     */
    private String extractPaymentId(JsonNode webhookData) {
        // Formato 1: {"resource":"122113012667","topic":"payment"}
        if (webhookData.has("resource")) {
            String resource = webhookData.get("resource").asText();
            
            // Verificar si es una URL de merchant_order
            if (resource.contains("merchant_orders/")) {
                String[] parts = resource.split("/");
                return parts[parts.length - 1];
            } else {
                return resource;
            }
        }
        // Formato 2: {"data":{"id":"122113012667"}}
        else if (webhookData.has("data") && webhookData.get("data").has("id")) {
            return webhookData.get("data").get("id").asText();
        }
        // Formato 3: {"id":"123456","action":"payment.updated","type":"payment"}
        else if (webhookData.has("id")) {
            return webhookData.get("id").asText();
        }
        
        return null;
    }
    
    /**
     * Obtener información del pago desde Mercado Pago
     */
    private Payment getPaymentFromMercadoPago(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(paymentId));
        } catch (MPApiException e) {
            logger.error("[MP-Webhook] API error getting payment: {}", e.getMessage());
            return null;
        } catch (MPException e) {
            logger.error("[MP-Webhook] Connection error: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Procesar el estado del pago
     */
    private void processPaymentStatus(Payment payment) {
        String paymentId = payment.getId().toString();
        String status = payment.getStatus();
        
        logger.info("[MP-Webhook] Processing payment {} with status: {}", paymentId, status);
        
        try {
            // Buscar la transacción
            PaymentTransaction transaction = findTransaction(paymentId, payment.getExternalReference());
            
            if (transaction == null) {
                logger.error("[MP-Webhook] Transaction not found for paymentId: {}", paymentId);
                return;
            }
            
            logger.info("[MP-Webhook] Transaction found: ID={}, Account={}, Coins={}, Current Status={}, ProcessedAt={}", 
                       transaction.getId(), transaction.getAccount().getId(), transaction.getCoinsAmount(),
                       transaction.getStatus(), transaction.getProcessedAt());
            
            // Verificar si ya fue procesada (evitar procesamiento duplicado)
            if (transaction.getProcessedAt() != null && PaymentStatus.APPROVED.equals(transaction.getStatus())) {
                logger.info("[MP-Webhook] Transaction {} already processed. Skipping.", transaction.getId());
                return;
            }
            
            // Actualizar el ID del pago en la transacción
            transaction.setMpPaymentId(paymentId);
            
            // Procesar según el estado
            switch (status) {
                case "approved":
                    processApprovedPayment(transaction);
                    break;
                case "rejected":
                    transaction.setStatus(PaymentStatus.REJECTED);
                    break;
                case "pending":
                    transaction.setStatus(PaymentStatus.PENDING);
                    break;
                case "in_process":
                    transaction.setStatus(PaymentStatus.IN_PROCESS);
                    break;
                case "cancelled":
                    transaction.setStatus(PaymentStatus.CANCELLED);
                    break;
                default:
                    logger.warn("[MP-Webhook] Unhandled payment status: {}", status);
                    return;
            }
            
            transaction.setUpdatedAt(new Date());
            paymentTransactionRepository.save(transaction);
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] Error processing payment status: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Buscar transacción por diferentes criterios
     */
    private PaymentTransaction findTransaction(String paymentId, String externalRef) {
        // Buscar por paymentId
        PaymentTransaction transaction = paymentTransactionRepository.findByMpPaymentId(paymentId)
                .orElse(null);
        
        if (transaction != null) {
            return transaction;
        }
        
        // Buscar por external_reference (preference_id o UUID)
        if (externalRef != null && !externalRef.isEmpty()) {
            logger.info("[MP-Webhook] Searching by external_reference: {}", externalRef);
            
            // Intentar como preference_id primero
            transaction = paymentTransactionRepository.findByMpPreferenceId(externalRef)
                    .orElse(null);
            
            // Si no, intentar como UUID
            if (transaction == null) {
                transaction = paymentTransactionRepository.findByExternalUuid(externalRef)
                        .orElse(null);
            }
        }
        
        // Buscar transacciones recientes sin paymentId
        if (transaction == null) {
            logger.info("[MP-Webhook] Searching recent transactions without paymentId");
            Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            List<PaymentTransaction> recentTransactions = paymentTransactionRepository.findByDateRange(oneDayAgo, new Date());
            
            transaction = recentTransactions.stream()
                    .filter(t -> t.getMpPaymentId() == null || t.getMpPaymentId().isEmpty())
                    .filter(t -> t.getStatus() == PaymentStatus.PENDING)
                    .findFirst()
                    .orElse(null);
        }
        
        return transaction;
    }
    
    /**
     * Procesar pago aprobado - SUMAR MONEDAS
     * CRITICAL: Must be called within a REPEATABLE_READ transaction
     */
    private void processApprovedPayment(PaymentTransaction transaction) {
        // Verificar que no se haya procesado ya
        if (PaymentStatus.APPROVED.equals(transaction.getStatus()) || transaction.getProcessedAt() != null) {
            logger.warn("[MP-Webhook] Transaction already processed: ID={}, Status={}, ProcessedAt={}", 
                       transaction.getId(), transaction.getStatus(), transaction.getProcessedAt());
            return;
        }
        
        logger.info("[MP-Webhook] Processing approved payment for transaction: {}", transaction.getId());
        
        // Actualizar estado y marcar como procesado
        transaction.setStatus(PaymentStatus.APPROVED);
        transaction.markAsProcessed();
        
        // Guardar ANTES de agregar monedas
        paymentTransactionRepository.save(transaction);
        paymentTransactionRepository.flush(); // Force immediate write
        
        // SUMAR MONEDAS A LA CUENTA
        AccountMaster account = transaction.getAccount();
        Integer currentCoins = account.getTerraCoins();
        
        if (currentCoins == null) {
            currentCoins = 0;
        }
        
        Integer newCoins = currentCoins + transaction.getCoinsAmount();
        
        logger.info("[MP-Webhook] Adding {} coins to account {}. Total: {} -> {}", 
                   transaction.getCoinsAmount(), account.getId(), currentCoins, newCoins);
        
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush(); // Force immediate write
        
        // Registrar en auditoría (si falla, hace rollback de TODO)
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "mercadopago");
    }
}

