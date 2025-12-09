package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentAuditService;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
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
 * PayPal payment strategy implementation
 */
@Component
public class PayPalStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(PayPalStrategy.class);
    
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
    
    private PayPalHttpClient client;
    
    @Override
    public String getProviderName() {
        return "paypal";
    }
    
    @Override
    public boolean supports(String provider) {
        return "paypal".equalsIgnoreCase(provider) || "pp".equalsIgnoreCase(provider);
    }
    
    /**
     * Get or create PayPal HTTP client
     */
    private PayPalHttpClient getClient() {
        if (client == null) {
            PayPalEnvironment environment;
            
            if ("live".equalsIgnoreCase(mode)) {
                logger.info("[PayPal] Using LIVE environment");
                environment = new PayPalEnvironment.Live(liveClientId, liveClientSecret);
            } else {
                logger.info("[PayPal] Using SANDBOX environment");
                environment = new PayPalEnvironment.Sandbox(sandboxClientId, sandboxClientSecret);
            }
            
            client = new PayPalHttpClient(environment);
        }
        
        return client;
    }
    
    @Override
    public PaymentPreferenceResponse createPayment(PaymentTransaction transaction) throws Exception {
        logger.info("[PayPal] Creating order for transaction: {}", transaction.getId());
        
        try {
            CoinPackage coinPackage = transaction.getCoinPackage();
            
            // Crear la orden de PayPal
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");
            
            // Configurar purchase unit
            List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .referenceId("account_" + transaction.getAccount().getId() + "_package_" + coinPackage.getId())
                    .description(coinPackage.getDescription())
                    .customId(transaction.getExternalUuid()) // UUID en lugar de ID secuencial
                    .amountWithBreakdown(new AmountWithBreakdown()
                            .currencyCode("USD") // PayPal usa USD como moneda principal
                            .value(coinPackage.getPrice().toString()));
            
            purchaseUnits.add(purchaseUnit);
            orderRequest.purchaseUnits(purchaseUnits);
            
            // Configurar contexto de aplicación
            ApplicationContext applicationContext = new ApplicationContext()
                    .brandName("Terra Lineage 2")
                    .landingPage("BILLING")
                    .shippingPreference("NO_SHIPPING")
                    .userAction("PAY_NOW");
            
            orderRequest.applicationContext(applicationContext);
            
            // Crear la orden en PayPal
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.requestBody(orderRequest);
            
            HttpResponse<Order> response = getClient().execute(request);
            Order order = response.result();
            
            logger.info("[PayPal] Order created: {}", order.id());
            
            // Guardar el ID de la orden en la transacción
            transaction.setPaypalOrderId(order.id());
            transaction.setStatus(PaymentStatus.PENDING);
            paymentTransactionRepository.save(transaction);
            
            // Obtener el link de aprobación
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse(null);
            
            // Retornar respuesta
            return new PaymentPreferenceResponse(
                    order.id(),
                    approvalUrl,
                    approvalUrl,
                    null // PayPal no usa public key como MercadoPago
            );
            
        } catch (Exception e) {
            logger.error("[PayPal] Error creating order: {}", e.getMessage(), e);
            throw new Exception("Error al crear orden en PayPal: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public PaymentTransaction capturePayment(String orderId) throws Exception {
        logger.info("[PayPal] Capturing order: {}", orderId);
        
        try {
            // Capturar la orden
            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            HttpResponse<Order> response = getClient().execute(request);
            Order order = response.result();
            
            logger.info("✅ [PayPal] Order captured: {}, status: {}", order.id(), order.status());
            
            // Buscar la transacción
            PaymentTransaction transaction = paymentTransactionRepository.findByPaypalOrderId(orderId)
                    .orElseThrow(() -> new Exception("Transaction not found for orderId: " + orderId));
            
            logger.info("🔍 [PayPal] Transaction found - ID: {}, Current Status: {}", 
                       transaction.getId(), transaction.getStatus());
            
            // SECURITY: Validar que el paquete siga activo
            if (!transaction.getCoinPackage().isActive()) {
                logger.error("[PayPal] Package is no longer active for transaction: {}", transaction.getId());
                throw new IllegalStateException("Package is no longer available");
            }
            
            // Actualizar estado según PayPal
            updateTransactionStatus(transaction, order);
            
            // Guardar explícitamente después de actualizar
            transaction = paymentTransactionRepository.save(transaction);
            paymentTransactionRepository.flush();
            
            logger.info("💾 [PayPal] Transaction saved - Final Status: {}", transaction.getStatus());
            
            return transaction;
            
        } catch (Exception e) {
            logger.error("[PayPal] Error capturing order: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean refundPayment(String orderId) throws Exception {
        logger.info("[PayPal] Refunding order: {}", orderId);
        
        try {
            // TODO: Implementar lógica de reembolso de PayPal
            // Ver: https://developer.paypal.com/docs/api/payments/v2/#captures_refund
            
            logger.warn("[PayPal] Refund not implemented yet");
            return false;
        } catch (Exception e) {
            logger.error("[PayPal] Error refunding order: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Actualizar estado de transacción según la orden de PayPal
     * CRITICAL: Este método debe ejecutarse dentro de una transacción SERIALIZABLE
     */
    private void updateTransactionStatus(PaymentTransaction transaction, Order order) {
        String status = order.status();
        
        logger.info("[PayPal] Updating transaction status to: {}", status);
        
        switch (status) {
            case "COMPLETED":
                // Verificar DOBLEMENTE que no se haya procesado ya
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    logger.info("[PayPal] Processing payment for the first time");
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed(); // Marcar como procesado ANTES de agregar monedas
                    
                    // Guardar primero para evitar race condition
                    transaction.setPaypalOrderId(order.id());
                    transaction.setUpdatedAt(new Date());
                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush(); // Forzar escritura a BD
                    
                    // Ahora sí agregar monedas
                    addCoinsToAccount(transaction);
                } else {
                    logger.warn("[PayPal] Payment already processed. Status: {}, ProcessedAt: {}", 
                               transaction.getStatus(), transaction.getProcessedAt());
                }
                break;
            case "APPROVED":
                // Cuando PayPal retorna APPROVED después de capture, procesar igual que COMPLETED
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus()) && transaction.getProcessedAt() == null) {
                    logger.info("[PayPal] Processing APPROVED payment for the first time");
                    transaction.setStatus(PaymentStatus.APPROVED);
                    transaction.markAsProcessed();
                    
                    transaction.setPaypalOrderId(order.id());
                    transaction.setUpdatedAt(new Date());
                    paymentTransactionRepository.save(transaction);
                    paymentTransactionRepository.flush();
                    
                    addCoinsToAccount(transaction);
                } else {
                    logger.warn("[PayPal] Payment already processed (APPROVED). Status: {}, ProcessedAt: {}", 
                               transaction.getStatus(), transaction.getProcessedAt());
                }
                break;
            case "VOIDED":
            case "CANCELLED":
                transaction.setStatus(PaymentStatus.CANCELLED);
                break;
            case "CREATED":
            case "SAVED":
                transaction.setStatus(PaymentStatus.PENDING);
                break;
            default:
                logger.warn("[PayPal] Unhandled order status: {}", status);
                break;
        }
        
        transaction.setPaypalOrderId(order.id());
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
    }
    
    /**
     * Obtener el estado de una orden de PayPal
     */
    public String getOrderStatus(String orderId) throws Exception {
        try {
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            HttpResponse<Order> response = getClient().execute(request);
            Order order = response.result();
            return order.status();
        } catch (Exception e) {
            logger.error("[PayPal] Error getting order status for {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Obtener el approval URL de una orden de PayPal existente
     * Útil para reanudar pagos pendientes
     */
    public String getApprovalUrl(String orderId) throws Exception {
        try {
            logger.info("[PayPal] Getting approval URL for order: {}", orderId);
            
            // Obtener la orden de PayPal
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            HttpResponse<Order> response = getClient().execute(request);
            Order order = response.result();
            
            // Obtener el link de aprobación (puede estar disponible incluso si está APPROVED)
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse(null);
            
            // Si hay approval URL disponible, retornarlo (incluso si la orden está APPROVED)
            if (approvalUrl != null) {
                logger.info("[PayPal] Approval URL found for order {} (status: {}): {}", orderId, order.status(), approvalUrl);
                return approvalUrl;
            }
            
            // Si no hay approval URL, verificar el estado
            String status = order.status();
            if ("APPROVED".equals(status)) {
                // Si está APPROVED pero no hay approval URL, la orden ya fue aprobada
                // pero puede que falte capturar. Esto se manejará en el servicio.
                logger.warn("[PayPal] Order {} is APPROVED but no approval URL available", orderId);
                throw new IllegalArgumentException("Order is already approved. Payment should be processed automatically.");
            }
            
            if (!"CREATED".equals(status) && !"SAVED".equals(status)) {
                logger.warn("[PayPal] Order {} is in status {}, cannot resume", orderId, status);
                throw new IllegalArgumentException("Order is not in a resumable state. Status: " + status);
            }
            
            throw new IllegalArgumentException("Approval URL not found for order: " + orderId);
            
        } catch (Exception e) {
            logger.error("[PayPal] Error getting approval URL for order {}: {}", orderId, e.getMessage(), e);
            throw e;
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
        
        logger.info("[PayPal] Adding {} coins to account {}. Total: {} -> {}", 
                   transaction.getCoinsAmount(), account.getId(), currentCoins, newCoins);
        
        // Actualizar saldo
        account.setTerraCoins(newCoins);
        accountMasterRepository.save(account);
        accountMasterRepository.flush(); // Force immediate write
        
        // Registrar en auditoría (si falla, hace rollback de TODO)
        auditService.auditPurchase(account, currentCoins, newCoins, transaction, "paypal");
    }
    
}

