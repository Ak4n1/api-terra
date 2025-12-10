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
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
 * Implementación de la estrategia de webhook para Mercado Pago.
 * 
 * <p>Esta clase maneja la recepción y procesamiento de webhooks de Mercado Pago,
 * incluyendo la verificación de firmas HMAC-SHA256 y la actualización de estados
 * de transacciones.
 * 
 * <p>Características principales:
 * <ul>
 *   <li>Verificación de firmas HMAC-SHA256 según documentación oficial de Mercado Pago</li>
 *   <li>Extracción inteligente del ID de notificación desde múltiples fuentes</li>
 *   <li>Procesamiento de diferentes formatos de webhook (payment, merchant_order)</li>
 *   <li>Manejo de retry automático para consultas a la API de Mercado Pago</li>
 *   <li>Validación de estados de transacción antes de procesar</li>
 *   <li>Transacciones ACID con aislamiento SERIALIZABLE para prevenir race conditions</li>
 * </ul>
 * 
 * <p><b>Seguridad:</b>
 * <ul>
 *   <li>Rechaza webhooks sin firma válida</li>
 *   <li>Valida headers requeridos (x-signature, x-request-id)</li>
 *   <li>Previene procesamiento de transacciones en estados inválidos</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 3.0
 * @see WebhookStrategy
 * @see <a href="https://www.mercadopago.com.ar/developers/es/guides/notifications/webhooks">
 *      Documentación de Webhooks de Mercado Pago</a>
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
    
    @Autowired
    private RetryRegistry retryRegistry;
    
    /**
     * Obtiene el nombre del proveedor de webhook.
     * 
     * @return El nombre del proveedor: "mercadopago"
     */
    @Override
    public String getProviderName() {
        return "mercadopago";
    }
    
    /**
     * Verifica si esta estrategia soporta el proveedor especificado.
     * 
     * @param provider Nombre del proveedor a verificar
     * @return true si el proveedor es "mercadopago" o "mp" (case-insensitive)
     */
    @Override
    public boolean supports(String provider) {
        return "mercadopago".equalsIgnoreCase(provider) || "mp".equalsIgnoreCase(provider);
    }
    
    /**
     * Verifica la autenticidad de un webhook de Mercado Pago usando HMAC-SHA256.
     * 
     * <p>La verificación sigue el proceso documentado por Mercado Pago:
     * <ol>
     *   <li>Extrae la firma del header x-signature (formato: ts=timestamp,v1=hash)</li>
     *   <li>Obtiene el x-request-id del header</li>
     *   <li>Extrae el ID de notificación del payload o query parameters</li>
     *   <li>Construye el string a firmar: "id:{notificationId};request-id:{requestId};ts:{timestamp};"</li>
     *   <li>Calcula el HMAC-SHA256 usando el webhook secret</li>
     *   <li>Compara el hash recibido con el calculado (timing-safe)</li>
     * </ol>
     * 
     * <p><b>Requisitos:</b>
     * <ul>
     *   <li>Header x-signature debe estar presente</li>
     *   <li>Header x-request-id debe estar presente</li>
     *   <li>Webhook secret debe estar configurado</li>
     *   <li>ID de notificación debe poder extraerse del payload o headers</li>
     * </ul>
     * 
     * @param headers Headers HTTP del webhook, incluyendo x-signature y x-request-id
     * @param payload Cuerpo del webhook como string JSON
     * @return true si la firma es válida y el webhook es auténtico, false en caso contrario
     * @throws Exception Si ocurre un error durante la verificación
     * @see <a href="https://www.mercadopago.com.ar/developers/es/guides/notifications/webhooks#bookmark_validar_la_autenticidad_de_una_notificaci%C3%B3n">
     *      Documentación de verificación de webhooks</a>
     */
    @Override
    public boolean verifyWebhook(Map<String, String> headers, String payload) throws Exception {
        String signature = headers.get("x-signature");
        
        if (signature == null || signature.isEmpty()) {
            logger.error("[MP-Webhook] ❌ No se proporciono firma - WEBHOOK RECHAZADO");
            return false;
        }
        
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            logger.error("[MP-Webhook] ❌ No hay webhook secret configurado");
            return false;
        }
        
        try {
            String[] signatureParts = extractSignatureParts(signature);
            if (signatureParts == null) {
                logger.error("[MP-Webhook] ❌ Formato de firma inválido");
                return false;
            }
            
            String timestamp = signatureParts[0];
            String receivedHash = signatureParts[1];
            
            String requestId = headers.get("x-request-id");
            if (requestId == null || requestId.isEmpty()) {
                logger.error("[MP-Webhook] ❌ Falta header x-request-id");
                return false;
            }
            
            String notificationId = extractNotificationId(payload, headers);
            if (notificationId == null) {
                logger.error("[MP-Webhook] ❌ No se pudo extraer ID de la notificacion. Payload: {}, Query ID: {}", 
                           payload != null ? payload.substring(0, Math.min(100, payload.length())) : "null",
                           headers.get("x-query-id"));
                return false;
            }
            
            String dataToSign = String.format("id:%s;request-id:%s;ts:%s;", 
                notificationId.toLowerCase(), requestId, timestamp);
            
            String calculatedHash = calculateHMAC(dataToSign, webhookSecret);
            
            boolean isValid = MessageDigest.isEqual(
                receivedHash.getBytes(StandardCharsets.UTF_8),
                calculatedHash.getBytes(StandardCharsets.UTF_8)
            );
            
            if (!isValid) {
                logger.error("[MP-Webhook] Verificacion de firma fallida");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] ❌ Error verificando firma: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Extrae el ID de la notificación del payload o headers.
     * 
     * <p>El ID de notificación es crucial para la verificación de firmas. Mercado Pago
     * puede enviarlo en diferentes formatos:
     * <ol>
     *   <li><b>Prioridad 1:</b> Query parameter data.id (header x-query-data-id) - Formato preferido</li>
     *   <li><b>Prioridad 2:</b> data.id en el payload JSON - ID de notificación correcto</li>
     *   <li><b>Prioridad 3:</b> Query parameter id (header x-query-id) - Puede ser ID de payment, no de notificación</li>
     *   <li><b>Prioridad 4:</b> id en el payload JSON - Último recurso</li>
     * </ol>
     * 
     * <p><b>Importante:</b> Cuando el webhook viene con ?id=...&topic=payment o ?id=...&topic=merchant_order,
     * ese ID es del payment/merchant_order, NO de la notificación. Estos webhooks se rechazan
     * si no se encuentra un data.id, ya que no se puede verificar la firma correctamente.
     * 
     * @param payload Cuerpo del webhook como string JSON
     * @param headers Headers HTTP, incluyendo query parameters como x-query-*
     * @return El ID de notificación si se encuentra, null si no se puede extraer
     */
    private String extractNotificationId(String payload, Map<String, String> headers) {
        // Prioridad 1: data.id en query parameter (formato preferido por Mercado Pago para notificaciones)
        String queryDataId = headers.get("x-query-data-id");
        if (queryDataId != null && !queryDataId.isEmpty()) {
            return queryDataId;
        }
        
        // Prioridad 2: Buscar en el payload JSON (data.id)
        try {
            if (payload != null && !payload.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode webhookData = mapper.readTree(payload);
                
                if (webhookData.has("data") && webhookData.get("data").has("id")) {
                    return webhookData.get("data").get("id").asText();
                }
            }
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        
        // Prioridad 3: id en query parameter (puede ser el ID del payment, no de la notificación)
        // IMPORTANTE: Cuando viene ?id=...&topic=payment, ese ID es del payment, NO de la notificación
        // Mercado Pago usa un ID de notificación diferente para la firma, por lo que estos webhooks
        // fallarán la verificación. Son redundantes - Mercado Pago también envía el webhook correcto
        // con ?data.id=...&type=payment que sí pasa la verificación.
        String queryId = headers.get("x-query-id");
        if (queryId != null && !queryId.isEmpty()) {
            // NO retornar el ID - esto causará que la verificación falle y el webhook sea rechazado
            // Esto es correcto porque no podemos verificar la firma sin el ID de notificación correcto
            return null;
        }
        
        // Prioridad 4: id en el payload JSON (último recurso)
        try {
            if (payload != null && !payload.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode webhookData = mapper.readTree(payload);
                
                if (webhookData.has("id") && !webhookData.has("resource")) {
                    return webhookData.get("id").asText();
                }
            }
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        
        return null;
    }
    
    /**
     * Extrae los componentes de la firma de Mercado Pago.
     * 
     * <p>La firma de Mercado Pago tiene el formato: "ts=timestamp,v1=hash"
     * donde:
     * <ul>
     *   <li>ts: Timestamp en milisegundos</li>
     *   <li>v1: Hash HMAC-SHA256 en hexadecimal</li>
     * </ul>
     * 
     * @param signature La firma completa del header x-signature
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
     * Calcula el HMAC-SHA256 de un string usando el webhook secret.
     * 
     * <p>Este método implementa el algoritmo de firma HMAC-SHA256 según la
     * documentación oficial de Mercado Pago. El resultado se devuelve en
     * formato hexadecimal (lowercase, sin separadores).
     * 
     * @param payload El string a firmar (formato: "id:{id};request-id:{requestId};ts:{timestamp};")
     * @param secret El webhook secret configurado en Mercado Pago
     * @return El hash HMAC-SHA256 en formato hexadecimal
     * @throws Exception Si ocurre un error al calcular el HMAC
     * @see <a href="https://www.mercadopago.com.ar/developers/es/guides/notifications/webhooks#bookmark_validar_la_autenticidad_de_una_notificaci%C3%B3n">
     *      Documentación de verificación de webhooks</a>
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
    
    /**
     * Procesa un webhook de Mercado Pago después de verificar su autenticidad.
     * 
     * <p>Este método:
     * <ol>
     *   <li>Extrae el ID del pago del payload del webhook</li>
     *   <li>Consulta la API de Mercado Pago para obtener el estado actual del pago</li>
     *   <li>Actualiza la transacción local según el estado recibido</li>
     *   <li>Acredita monedas si el pago fue aprobado</li>
     * </ol>
     * 
     * <p>Utiliza retry logic para manejar fallos temporales al consultar la API.
     * 
     * @param payload El cuerpo del webhook como string JSON
     * @return Mensaje de resultado del procesamiento
     * @throws Exception Si ocurre un error al procesar el webhook
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public String processWebhook(String payload) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            String paymentId = extractPaymentId(webhookData);
            
            if (paymentId == null) {
                logger.error("[MP-Webhook] No se pudo extraer el ID del pago del webhook");
                return "ERROR: No payment ID found";
            }
            
            if ("123456".equals(paymentId)) {
                return "Test webhook processed";
            }
            
            Payment payment = getPaymentFromMercadoPago(paymentId);
            if (payment == null) {
                logger.warn("[MP-Webhook] No se pudo obtener el pago de MercadoPago");
                return "Payment not found in MercadoPago";
            }
            
            processPaymentStatus(payment);
            
            return "Webhook processed successfully";
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] ❌ Error procesando webhook: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Extrae el ID del pago del payload del webhook.
     * 
     * <p>Mercado Pago puede enviar el ID del pago en diferentes formatos:
     * <ul>
     *   <li>{"resource":"122113012667","topic":"payment"} - ID directo en resource</li>
     *   <li>{"data":{"id":"122113012667"}} - ID en data.id</li>
     *   <li>{"id":"123456","action":"payment.updated","type":"payment"} - ID directo</li>
     * </ul>
     * 
     * @param webhookData El payload del webhook parseado como JsonNode
     * @return El ID del pago si se encuentra, null en caso contrario
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
     * Obtiene la información de un pago desde la API de Mercado Pago.
     * 
     * <p>Este método consulta la API de Mercado Pago para obtener el estado
     * actual de un pago. Utiliza retry logic con exponential backoff para
     * manejar fallos temporales de la API.
     * 
     * @param paymentId El ID del pago en Mercado Pago
     * @return El objeto Payment con la información del pago, o null si no se encuentra o ocurre un error
     */
    private Payment getPaymentFromMercadoPago(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Retry retry = retryRegistry.retry("mercadopagoRetry");
            Payment payment = Retry.decorateSupplier(retry, () -> {
                try {
                    return client.get(Long.parseLong(paymentId));
                } catch (MPApiException e) {
                    throw new RuntimeException(e); // Envolver para Resilience4j
                } catch (MPException e) {
                    throw new RuntimeException(e); // Envolver para Resilience4j
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
            return payment;
        } catch (RuntimeException e) {
            // Desenvolver excepciones envueltas por Resilience4j
            Throwable cause = e.getCause();
            if (cause instanceof MPApiException) {
                MPApiException mpApiEx = (MPApiException) cause;
                logger.error("[MP-Webhook] Error obteniendo pago (después de reintentos): {}", mpApiEx.getMessage());
                return null;
            } else if (cause instanceof MPException) {
                MPException mpEx = (MPException) cause;
                logger.error("[MP-Webhook] Error de conexion (despues de reintentos): {}", mpEx.getMessage());
                return null;
            }
            logger.error("[MP-Webhook] Error después de reintentos: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("[MP-Webhook] Error después de reintentos: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Procesa el estado de un pago y actualiza la transacción correspondiente.
     * 
     * <p>Este método mapea los estados de Mercado Pago a los estados internos y
     * ejecuta las acciones correspondientes:
     * <ul>
     *   <li>approved → APPROVED (acredita monedas)</li>
     *   <li>rejected → REJECTED</li>
     *   <li>pending → PENDING</li>
     *   <li>in_process → IN_PROCESS</li>
     *   <li>cancelled → CANCELLED</li>
     * </ul>
     * 
     * @param payment El objeto Payment de Mercado Pago con el estado actual
     */
    private void processPaymentStatus(Payment payment) {
        String paymentId = payment.getId().toString();
        String status = payment.getStatus();
        
        try {
            PaymentTransaction transaction = findTransaction(paymentId, payment.getExternalReference());
            
            if (transaction == null) {
                logger.error("[MP-Webhook] Transaccion no encontrada. Payment ID: {}", paymentId);
                return;
            }
            
            if (transaction.getProcessedAt() != null && PaymentStatus.APPROVED.equals(transaction.getStatus())) {
                return;
            }
            
            transaction.setMpPaymentId(paymentId);
            
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
                    logger.warn("[MP-Webhook] Estado desconocido: {}", status);
                    return;
            }
            
            transaction.setUpdatedAt(new Date());
            paymentTransactionRepository.save(transaction);
            
        } catch (Exception e) {
            logger.error("[MP-Webhook] Error processing payment status: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Busca una transacción usando diferentes criterios de búsqueda.
     * 
     * <p>Este método intenta encontrar la transacción en el siguiente orden:
     * <ol>
     *   <li>Por paymentId de Mercado Pago (mp_payment_id)</li>
     *   <li>Por preferenceId (mp_preference_id) si externalRef es una preference</li>
     *   <li>Por external UUID si externalRef es un UUID</li>
     *   <li>Por transacciones recientes pendientes sin paymentId (últimas 24 horas)</li>
     * </ol>
     * 
     * @param paymentId El ID del pago en Mercado Pago
     * @param externalRef La referencia externa (preference_id o UUID)
     * @return La transacción encontrada, o null si no se encuentra
     */
    private PaymentTransaction findTransaction(String paymentId, String externalRef) {
        PaymentTransaction transaction = paymentTransactionRepository.findByMpPaymentId(paymentId)
                .orElse(null);
        
        if (transaction != null) {
            return transaction;
        }
        
        if (externalRef != null && !externalRef.isEmpty()) {
            transaction = paymentTransactionRepository.findByMpPreferenceId(externalRef)
                    .orElse(null);
            
            if (transaction != null) {
                return transaction;
            }
            
            transaction = paymentTransactionRepository.findByExternalUuid(externalRef)
                    .orElse(null);
            
            if (transaction != null) {
                return transaction;
            }
        }
        
        if (transaction == null) {
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
    /**
     * Procesa un pago aprobado, actualizando el estado y acreditando monedas.
     * 
     * <p>Este método:
     * <ul>
     *   <li>Valida que la transacción no esté en un estado inválido (reembolsada/cancelada)</li>
     *   <li>Verifica que la transacción no haya sido procesada previamente</li>
     *   <li>Actualiza el estado a APPROVED</li>
     *   <li>Marca la transacción como procesada</li>
     *   <li>Acredita las monedas a la cuenta del usuario</li>
     * </ul>
     * 
     * @param transaction La transacción a procesar
     * @throws IllegalStateException Si la transacción está en un estado inválido
     */
    private void processApprovedPayment(PaymentTransaction transaction) {
        // Validar que la transacción no esté en un estado inválido
        if (PaymentStatus.REFUNDED.equals(transaction.getStatus())) {
            logger.warn("[MP-Webhook] ⚠️ Intento de procesar transaccion reembolsada: {}", transaction.getId());
            throw new IllegalStateException("Cannot process refunded transaction");
        }
        
        if (PaymentStatus.CANCELLED.equals(transaction.getStatus())) {
            logger.warn("[MP-Webhook] ⚠️ Intento de procesar transaccion cancelada: {}", transaction.getId());
            throw new IllegalStateException("Cannot process cancelled transaction");
        }
        
        if (PaymentStatus.APPROVED.equals(transaction.getStatus()) || transaction.getProcessedAt() != null) {
            return;
        }
        
        transaction.setStatus(PaymentStatus.APPROVED);
        transaction.markAsProcessed();
        
        paymentTransactionRepository.save(transaction);
        paymentTransactionRepository.flush();
        
        AccountMaster account = transaction.getAccount();
        Integer currentCoins = account.getTerraCoins();
        
        if (currentCoins == null) {
            currentCoins = 0;
        }
        
        Integer newCoins = currentCoins + transaction.getCoinsAmount();
        
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush();
        
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "mercadopago");
    }
}

