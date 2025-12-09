package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentAuditService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MercadoPago payment strategy implementation
 */
@Component
public class MercadoPagoStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoStrategy.class);
    
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
    public PaymentPreferenceResponse createPayment(PaymentTransaction transaction) throws Exception {
        logger.info("[MP] Creando preferencia de pago para transacción: {}", transaction.getId());
        
        try {
            // Configurar Mercado Pago
            MercadoPagoConfig.setAccessToken(accessToken);
            
            // Crear cliente de preferencias
            PreferenceClient client = new PreferenceClient();
            
            CoinPackage coinPackage = transaction.getCoinPackage();
            
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
            
            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .externalReference("account_" + transaction.getAccount().getId() + "_package_" + coinPackage.getId())
                .notificationUrl(notificationUrl)
                .expires(true)
                .expirationDateFrom(java.time.OffsetDateTime.now())
                .expirationDateTo(java.time.OffsetDateTime.now().plusHours(24))
                .build();
            
            // Crear preferencia en Mercado Pago
            Preference preference = client.create(preferenceRequest);
            
            // Actualizar transacción con preferenceId
            transaction.setMpPreferenceId(preference.getId());
            paymentTransactionRepository.save(transaction);
            
            // Retornar respuesta
            return new PaymentPreferenceResponse(
                preference.getId(),
                preference.getInitPoint(),
                preference.getSandboxInitPoint(),
                publicKey
            );
            
        } catch (MPApiException e) {
            logger.error("[MP] Error de API: {} - {}", e.getApiResponse().getContent(), e.getMessage());
            throw new Exception("Error al crear preferencia en MercadoPago: " + e.getMessage());
        } catch (MPException e) {
            logger.error("[MP] Error de conexión: {}", e.getMessage());
            throw new Exception("Error de conexión con MercadoPago: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public PaymentTransaction capturePayment(String paymentId) throws Exception {
        logger.info("[MP] Capturando pago: {}", paymentId);
        
        // MercadoPago no requiere captura explícita, el webhook maneja todo
        // Este método se usa para verificar el estado
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            
            PaymentTransaction transaction = paymentTransactionRepository.findByMpPaymentId(paymentId)
                    .orElseThrow(() -> new Exception("Transacción no encontrada para paymentId: " + paymentId));
            
            // SECURITY: Validar que el paquete siga activo
            if (!transaction.getCoinPackage().isActive()) {
                logger.error("[MP] Package is no longer active for transaction: {}", transaction.getId());
                throw new IllegalStateException("Package is no longer available");
            }
            
            // Actualizar estado según Mercado Pago
            updateTransactionStatus(transaction, payment);
            
            return transaction;
        } catch (Exception e) {
            logger.error("[MP] Error capturando pago: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public boolean refundPayment(String paymentId) throws Exception {
        logger.info("[MP] Reembolsando pago: {}", paymentId);
        
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            // TODO: Implementar lógica de reembolso de MercadoPago
            // Ver: https://www.mercadopago.com.ar/developers/es/reference/chargebacks/_payments_id_refunds/post
            
            logger.warn("[MP] Reembolso no implementado aún");
            return false;
        } catch (Exception e) {
            logger.error("[MP] Error al reembolsar: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Actualizar estado de transacción según el pago de MercadoPago
     */
    private void updateTransactionStatus(PaymentTransaction transaction, Payment payment) {
        String status = payment.getStatus();
        
        switch (status) {
            case "approved":
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus())) {
                    transaction.setStatus(PaymentStatus.APPROVED);
                    addCoinsToAccount(transaction);
                }
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
        }
        
        transaction.setMpPaymentId(payment.getId().toString());
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
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
        
        logger.info("[MP] Agregando {} monedas a cuenta {}. Total: {} -> {}", 
                   transaction.getCoinsAmount(), account.getId(), currentCoins, newCoins);
        
        // Actualizar saldo
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush(); // Force immediate write
        
        // Registrar en auditoría (si falla, hace rollback de TODO)
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "mercadopago");
    }
}

