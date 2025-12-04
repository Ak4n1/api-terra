package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Implementación del servicio de Mercado Pago
 */
@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoServiceImpl.class);
    
    @Value("${mercadopago.access.token}")
    private String accessToken;
    
    @Value("${mercadopago.public.key}")
    private String publicKey;
    
    @Value("${mercadopago.notification.url}")
    private String notificationUrl;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Override
    public PaymentPreferenceResponse createPaymentPreference(CoinPackage coinPackage, Long accountId, String returnUrl, String cancelUrl) {
        logger.info("Creando preferencia de pago para paquete: {}, cuenta: {}", coinPackage.getId(), accountId);
        logger.info("URLs recibidas - Return: {}, Cancel: {}, Notification: {}", returnUrl, cancelUrl, notificationUrl);
        
        try {
            // Configurar Mercado Pago
            MercadoPagoConfig.setAccessToken(accessToken);
            logger.info("🔧 [MP] Access Token configurado: {}", accessToken.substring(0, 10) + "...");
            
            // Crear cliente de preferencias
            PreferenceClient client = new PreferenceClient();
            
            // Crear item de la preferencia
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(coinPackage.getId().toString())
                .title(coinPackage.getName())
                .description(coinPackage.getDescription())
                .quantity(1)
                .unitPrice(coinPackage.getPrice())
                .currencyId("ARS")
                .build();
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);
            
            // Validar que las URLs no sean null
            if (returnUrl == null || returnUrl.isEmpty()) {
                returnUrl = "http://localhost:4200/payment/success";
                logger.warn("Return URL era null, usando valor por defecto: {}", returnUrl);
            }
            if (cancelUrl == null || cancelUrl.isEmpty()) {
                cancelUrl = "http://localhost:4200/payment/cancel";
                logger.warn("Cancel URL era null, usando valor por defecto: {}", cancelUrl);
            }
            
            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .externalReference("account_" + accountId + "_package_" + coinPackage.getId())
                .notificationUrl(notificationUrl)
                .backUrls(com.mercadopago.client.preference.PreferenceBackUrlsRequest.builder()
                    .success(returnUrl)
                    .failure(cancelUrl)
                    .pending(cancelUrl)
                    .build())
                .expires(true)
                .expirationDateFrom(java.time.OffsetDateTime.now())
                .expirationDateTo(java.time.OffsetDateTime.now().plusHours(24))
                .build();
            
            // Crear preferencia en Mercado Pago
            Preference preference = client.create(preferenceRequest);
            
            // Retornar respuesta
            return new PaymentPreferenceResponse(
                preference.getId(),
                preference.getInitPoint(),
                preference.getSandboxInitPoint(),
                publicKey
            );
            
        } catch (MPApiException e) {
            logger.error("Error de API de Mercado Pago: {} - {}", e.getApiResponse().getContent(), e.getMessage());
            return new PaymentPreferenceResponse("error", "Error al crear preferencia en Mercado Pago: " + e.getMessage());
        } catch (MPException e) {
            logger.error("Error de Mercado Pago: {}", e.getMessage());
            return new PaymentPreferenceResponse("error", "Error de conexión con Mercado Pago: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al crear preferencia: {}", e.getMessage(), e);
            return new PaymentPreferenceResponse("error", "Error interno del servidor");
        }
    }
    
    @Override
    public boolean processWebhook(String payload, String signature) {
        logger.info("🟢 [WEBHOOK] ===== INICIANDO PROCESAMIENTO =====");
        logger.info("🟢 [WEBHOOK] Payload recibido: {}", payload);
        logger.info("🟢 [WEBHOOK] Signature: {}", signature);
        
        try {
            // Parsear el payload del webhook
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            logger.info("🟢 [WEBHOOK] JSON parseado correctamente");
            
            // Extraer el ID del pago - MÚLTIPLES FORMATOS DE MERCADO PAGO
            String paymentId = null;
            
            // Formato 1: {"resource":"122113012667","topic":"payment"} o {"resource":"https://api.mercadolibre.com/merchant_orders/33269353305","topic":"merchant_order"}
            if (webhookData.has("resource")) {
                String resource = webhookData.get("resource").asText();
                
                // Verificar si es una URL de merchant_order
                if (resource.contains("merchant_orders/")) {
                    // Extraer solo el ID numérico de la URL
                    String[] parts = resource.split("/");
                    paymentId = parts[parts.length - 1]; // Obtener el último elemento
                    logger.info("🟢 [WEBHOOK] Merchant Order ID extraído de URL: {}", paymentId);
                } else {
                    // Es un ID directo de payment
                    paymentId = resource;
                    logger.info("🟢 [WEBHOOK] Payment ID extraído de 'resource': {}", paymentId);
                }
            }
            // Formato 2: {"data":{"id":"122113012667"}} (formato alternativo)
            else if (webhookData.has("data") && webhookData.get("data").has("id")) {
                paymentId = webhookData.get("data").get("id").asText();
                logger.info("🟢 [WEBHOOK] Payment ID extraído de 'data.id': {}", paymentId);
            }
            // Formato 3: {"id":"123456","action":"payment.updated","type":"payment"} (formato de prueba)
            else if (webhookData.has("id")) {
                paymentId = webhookData.get("id").asText();
                logger.info("🟢 [WEBHOOK] Payment ID extraído de 'id': {}", paymentId);
            }
            
            if (paymentId == null) {
                logger.error("❌ [WEBHOOK] No se pudo extraer el ID del pago del webhook");
                logger.error("❌ [WEBHOOK] Estructura del payload: {}", webhookData.toString());
                return false;
            }
            
            // Verificar si es un webhook de prueba (ID 123456 es común en pruebas)
            if ("123456".equals(paymentId)) {
                return true;
            }
            
            // Obtener información del pago desde Mercado Pago
            Payment payment = getPaymentFromMercadoPago(paymentId);
            if (payment == null) {
                logger.warn("🟡 [WEBHOOK] No se pudo obtener el pago desde Mercado Pago: {}. Esto puede ser normal para merchant_orders", paymentId);
                return true;
            }
            
            // Procesar el pago según su estado
            boolean result = processPaymentStatus(payment);
            return result;
            
        } catch (Exception e) {
            logger.error("❌ [WEBHOOK] Error procesando webhook: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtener información del pago desde Mercado Pago
     */
    private Payment getPaymentFromMercadoPago(String paymentId) {
        try {
            // Configurar Mercado Pago
            MercadoPagoConfig.setAccessToken(accessToken);
            
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(paymentId));
        } catch (MPApiException e) {
            logger.error("Error obteniendo pago de Mercado Pago: {} - {}", 
                        e.getApiResponse().getContent(), e.getMessage());
            return null;
        } catch (MPException e) {
            logger.error("Error de conexión con Mercado Pago: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Procesar el estado del pago
     */
    private boolean processPaymentStatus(Payment payment) {
        String paymentId = payment.getId().toString();
        String status = payment.getStatus();
        
        logger.info("🟢 [WEBHOOK] Procesando pago {} con estado: {}", paymentId, status);
        
        try {
            // Buscar la transacción por paymentId primero
            PaymentTransaction transaction = paymentTransactionRepository.findByMpPaymentId(paymentId)
                    .orElse(null);
            
            if (transaction == null) {
                logger.warn("🟡 [WEBHOOK] No se encontró transacción por paymentId: {}. Buscando por otros criterios...", paymentId);
                
                // Buscar por external_reference del pago (puede ser preferenceId o account_package_info)
                String externalRef = payment.getExternalReference();
                if (externalRef != null && !externalRef.isEmpty()) {
                    logger.info("🟡 [WEBHOOK] Buscando transacción por external_reference: {}", externalRef);
                    
                    // Intentar buscar por preferenceId
                    transaction = paymentTransactionRepository.findByMpPreferenceId(externalRef)
                            .orElse(null);
                    
                    // Si no se encuentra, buscar por external_reference
                    if (transaction == null) {
                        transaction = paymentTransactionRepository.findByExternalReference(externalRef)
                                .orElse(null);
                    }
                }
                
                // Si aún no se encuentra, buscar transacciones recientes sin paymentId asignado
                if (transaction == null) {
                    logger.info("🟡 [WEBHOOK] Buscando transacciones recientes sin paymentId asignado...");
                    // Buscar transacciones pendientes recientes (últimas 24 horas)
                    Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
                    List<PaymentTransaction> recentTransactions = paymentTransactionRepository.findByDateRange(oneDayAgo, new Date());
                    
                    // Filtrar transacciones sin paymentId asignado
                    transaction = recentTransactions.stream()
                            .filter(t -> t.getMpPaymentId() == null || t.getMpPaymentId().isEmpty())
                            .filter(t -> t.getStatus() == PaymentStatus.PENDING)
                            .findFirst()
                            .orElse(null);
                    
                    if (transaction != null) {
                        logger.info("🟡 [WEBHOOK] Transacción reciente encontrada: ID={}, PreferenceId={}", 
                                  transaction.getId(), transaction.getMpPreferenceId());
                    }
                }
            }
            
            if (transaction == null) {
                logger.error("❌ [WEBHOOK] No se encontró transacción para paymentId: {} ni por otros criterios", paymentId);
                return false;
            }
            
            logger.info("🟢 [WEBHOOK] Transacción encontrada: ID={}, Account={}, Coins={}, Status={}", 
                       transaction.getId(), transaction.getAccount().getId(), 
                       transaction.getCoinsAmount(), transaction.getStatus());
            
            // Actualizar el ID del pago en la transacción
            transaction.setMpPaymentId(paymentId);
            logger.info("🟢 [WEBHOOK] PaymentId actualizado en transacción: {}", paymentId);
            
            switch (status) {
                case "approved":
                    logger.info("🟢 [WEBHOOK] Procesando pago APROBADO");
                    return processApprovedPayment(transaction, payment);
                case "rejected":
                    logger.info("🟢 [WEBHOOK] Procesando pago RECHAZADO");
                    return processRejectedPayment(transaction, payment);
                case "pending":
                    logger.info("🟢 [WEBHOOK] Procesando pago PENDIENTE");
                    return processPendingPayment(transaction, payment);
                case "in_process":
                    logger.info("🟢 [WEBHOOK] Procesando pago EN PROCESO");
                    return processInProcessPayment(transaction, payment);
                case "cancelled":
                    logger.info("🟢 [WEBHOOK] Procesando pago CANCELADO");
                    return processCancelledPayment(transaction, payment);
                default:
                    logger.warn("🟡 [WEBHOOK] Estado de pago no manejado: {}", status);
                    return false;
            }
            
        } catch (Exception e) {
            logger.error("Error procesando estado del pago {}: {}", paymentId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Procesar pago aprobado - SUMAR MONEDAS
     */
    private boolean processApprovedPayment(PaymentTransaction transaction, Payment payment) {
        try {
            logger.info("💰 [APPROVED] ===== PROCESANDO PAGO APROBADO =====");
            logger.info("💰 [APPROVED] Transacción ID: {}", transaction.getId());
            logger.info("💰 [APPROVED] Account ID: {}", transaction.getAccount().getId());
            logger.info("💰 [APPROVED] Coins a agregar: {}", transaction.getCoinsAmount());
            logger.info("💰 [APPROVED] Estado actual: {}", transaction.getStatus());
            
            // Verificar que no se haya procesado ya
            if (PaymentStatus.APPROVED.equals(transaction.getStatus())) {
                logger.info("💰 [APPROVED] Transacción ya procesada como aprobada: {}", transaction.getId());
                return true;
            }
            
            // Actualizar estado de la transacción
            logger.info("💰 [APPROVED] Actualizando estado de transacción a APPROVED");
            transaction.setStatus(PaymentStatus.APPROVED);
            transaction.setUpdatedAt(new Date());
            paymentTransactionRepository.save(transaction);
            
            // SUMAR MONEDAS A LA CUENTA
            AccountMaster account = transaction.getAccount();
            Integer currentCoins = account.getTerraCoins();
            Integer newCoins = currentCoins + transaction.getCoinsAmount();
            
            account.setTerraCoins(newCoins);
            accountMasterRepository.save(account);
            
            return true;
            
        } catch (Exception e) {
            logger.error("❌ [APPROVED] Error procesando pago aprobado: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Procesar pago rechazado
     */
    private boolean processRejectedPayment(PaymentTransaction transaction, Payment payment) {
        logger.info("Procesando pago rechazado para transacción: {}", transaction.getId());
        
        transaction.setStatus(PaymentStatus.REJECTED);
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        
        return true;
    }
    
    /**
     * Procesar pago pendiente
     */
    private boolean processPendingPayment(PaymentTransaction transaction, Payment payment) {
        logger.info("Procesando pago pendiente para transacción: {}", transaction.getId());
        
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        
        return true;
    }
    
    /**
     * Procesar pago en proceso
     */
    private boolean processInProcessPayment(PaymentTransaction transaction, Payment payment) {
        logger.info("Procesando pago en proceso para transacción: {}", transaction.getId());
        
        transaction.setStatus(PaymentStatus.IN_PROCESS);
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        
        return true;
    }
    
    /**
     * Procesar pago cancelado
     */
    private boolean processCancelledPayment(PaymentTransaction transaction, Payment payment) {
        logger.info("Procesando pago cancelado para transacción: {}", transaction.getId());
        
        transaction.setStatus(PaymentStatus.CANCELLED);
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        
        return true;
    }
    
    @Override
    public Object getPaymentInfo(String paymentId) {
        logger.info("Obteniendo información del pago: {}", paymentId);
        
        // TODO: Implementar obtención real de información de pago
        return null;
    }
    
    @Override
    public String getPaymentStatus(String paymentId) {
        logger.info("Verificando estado del pago: {}", paymentId);
        
        // TODO: Implementar verificación real del estado
        return "pending";
    }
    
    @Override
    public boolean refundPayment(String paymentId, String reason) {
        logger.info("Reembolsando pago: {}, razón: {}", paymentId, reason);
        
        // TODO: Implementar reembolso real
        return false;
    }
}
