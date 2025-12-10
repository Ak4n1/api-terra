package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentAuditService;
import com.ak4n1.terra.api.terra_api.payments.services.CoinService;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
 * Implementación de la estrategia de pago para PayPal.
 * 
 * <p>Esta clase maneja la creación de órdenes de pago, captura de pagos y
 * actualización de estados de transacciones usando la API de PayPal.
 * 
 * <p>Características principales:
 * <ul>
 *   <li>Creación de órdenes de pago con PayPal Checkout</li>
 *   <li>Captura de pagos después de la aprobación del usuario</li>
 *   <li>Soporte para entornos sandbox y producción</li>
 *   <li>Manejo de retry automático con Resilience4j para llamadas a la API</li>
 *   <li>Transacciones ACID con aislamiento SERIALIZABLE para prevenir race conditions</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 3.0
 * @see PaymentStrategy
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
    
    @Autowired
    private RetryRegistry retryRegistry;
    
    @Autowired
    private CoinService coinService;
    
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
     * Obtiene o crea el cliente HTTP de PayPal.
     * 
     * <p>El cliente se crea según el modo configurado (sandbox o live) y se reutiliza
     * para todas las llamadas subsecuentes a la API de PayPal.
     * 
     * @return El cliente HTTP de PayPal configurado para el entorno actual
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
    
    /**
     * Crea una orden de pago en PayPal.
     * 
     * <p>Este método crea una orden de pago en PayPal con los siguientes detalles:
     * <ul>
     *   <li>Item basado en el paquete de monedas solicitado</li>
     *   <li>Monto y moneda del paquete</li>
     *   <li>URLs de retorno y cancelación</li>
     *   <li>Custom ID con información de la transacción</li>
     * </ul>
     * 
     * <p>La creación de la orden utiliza retry logic con exponential backoff
     * para manejar fallos temporales de la API de PayPal.
     * 
     * @param transaction La transacción de pago con los detalles del paquete y cuenta
     * @return Respuesta con el ID de orden y URL de aprobación para el usuario
     * @throws Exception Si ocurre un error al crear la orden o si la API de PayPal falla
     */
    @Override
    public PaymentPreferenceResponse createPayment(PaymentTransaction transaction) throws Exception {
        logger.debug("[PayPal] Creating order");
        
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
            
            // Crear la orden en PayPal con retry
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.requestBody(orderRequest);
            
            Retry retry = retryRegistry.retry("paypalRetry");
            HttpResponse<Order> response = Retry.decorateSupplier(retry, () -> {
                try {
                    return getClient().execute(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
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
    
    /**
     * Captura un pago de PayPal después de que el usuario lo aprueba.
     * 
     * <p>Este método captura el pago de una orden de PayPal que ha sido aprobada
     * por el usuario. Después de la captura exitosa:
     * <ul>
     *   <li>Actualiza el estado de la transacción a APPROVED</li>
     *   <li>Acredita las monedas a la cuenta del usuario</li>
     *   <li>Registra la operación en auditoría</li>
     * </ul>
     * 
     * <p>La captura utiliza retry logic para manejar fallos temporales de la API.
     * 
     * <p><b>CRÍTICO:</b> Utiliza aislamiento SERIALIZABLE para prevenir condiciones
     * de carrera al acreditar monedas.
     * 
     * @param orderId El ID de la orden de PayPal a capturar
     * @return La transacción actualizada con el estado APPROVED
     * @throws Exception Si la orden no se encuentra, ya fue capturada, o si ocurre un error
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public PaymentTransaction capturePayment(String orderId) throws Exception {
        logger.debug("[PayPal] Capturing order");
        
        try {
            // Capturar la orden con retry
            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            Retry retry = retryRegistry.retry("paypalRetry");
            HttpResponse<Order> response = Retry.decorateSupplier(retry, () -> {
                try {
                    return getClient().execute(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
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
    
    /**
     * Reembolsa un pago de PayPal.
     * 
     * <p><b>NOTA:</b> Esta funcionalidad aún no está completamente implementada.
     * 
     * @param orderId El ID de la orden de PayPal a reembolsar
     * @return false ya que la funcionalidad no está implementada
     * @throws Exception Si ocurre un error
     */
    @Override
    public boolean refundPayment(String orderId) throws Exception {
        logger.debug("[PayPal] Refunding order");
        
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
     * Actualiza el estado de una transacción según el estado de la orden en PayPal.
     * 
     * <p>Este método mapea los estados de PayPal a los estados internos del sistema
     * y procesa la acreditación de monedas cuando el pago es completado.
     * 
     * <p>Estados mapeados:
     * <ul>
     *   <li>COMPLETED → APPROVED (acredita monedas)</li>
     *   <li>APPROVED → APPROVED (acredita monedas si aún no procesado)</li>
     *   <li>VOIDED/CANCELLED → CANCELLED</li>
     *   <li>CREATED/SAVED → PENDING</li>
     * </ul>
     * 
     * <p><b>CRÍTICO:</b> Este método debe ejecutarse dentro de una transacción
     * SERIALIZABLE para prevenir condiciones de carrera.
     * 
     * <p><b>Validaciones de seguridad:</b>
     * <ul>
     *   <li>Previene procesar transacciones ya reembolsadas</li>
     *   <li>Previene procesar transacciones canceladas</li>
     *   <li>Evita acreditar monedas múltiples veces (verifica processedAt)</li>
     * </ul>
     * 
     * @param transaction La transacción local a actualizar
     * @param order El objeto Order de PayPal con el estado actual
     */
    private void updateTransactionStatus(PaymentTransaction transaction, Order order) {
        String status = order.status();
        
        logger.info("[PayPal] Updating transaction status to: {}", status);
        
        switch (status) {
            case "COMPLETED":
                // Verificar DOBLEMENTE que no se haya procesado ya
                // Validar que la transacción no esté en un estado inválido
                if (PaymentStatus.REFUNDED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal] ⚠️ Intento de procesar transaccion reembolsada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process refunded transaction");
                }
                
                if (PaymentStatus.CANCELLED.equals(transaction.getStatus())) {
                    logger.warn("[PayPal] ⚠️ Intento de procesar transaccion cancelada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process cancelled transaction");
                }
                
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
     * Obtiene el estado actual de una orden de PayPal.
     * 
     * <p>Este método consulta la API de PayPal para obtener el estado más reciente
     * de una orden. Utiliza retry logic para manejar fallos temporales.
     * 
     * @param orderId El ID de la orden de PayPal
     * @return El estado de la orden (COMPLETED, APPROVED, PENDING, etc.)
     * @throws Exception Si la orden no se encuentra o si ocurre un error al consultar la API
     */
    public String getOrderStatus(String orderId) throws Exception {
        try {
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            Retry retry = retryRegistry.retry("paypalRetry");
            HttpResponse<Order> response = Retry.decorateSupplier(retry, () -> {
                try {
                    return getClient().execute(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
            Order order = response.result();
            return order.status();
        } catch (Exception e) {
            logger.error("[PayPal] Error getting order status for {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Obtiene la URL de aprobación de una orden de PayPal existente.
     * 
     * <p>Este método es útil para reanudar pagos pendientes. Consulta la orden
     * en PayPal y extrae el link de aprobación para que el usuario pueda completar
     * el pago.
     * 
     * <p>Utiliza retry logic para manejar fallos temporales de la API.
     * 
     * @param orderId El ID de la orden de PayPal
     * @return La URL de aprobación para que el usuario complete el pago, o null si no está disponible
     * @throws Exception Si la orden no se encuentra o si ocurre un error al consultar la API
     */
    public String getApprovalUrl(String orderId) throws Exception {
        try {
            logger.debug("[PayPal] Getting approval URL");
            
            // Obtener la orden de PayPal con retry
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            Retry retry = retryRegistry.retry("paypalRetry");
            HttpResponse<Order> response = Retry.decorateSupplier(retry, () -> {
                try {
                    return getClient().execute(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
            Order order = response.result();
            
            // Obtener el link de aprobación (puede estar disponible incluso si está APPROVED)
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse(null);
            
            // Si hay approval URL disponible, retornarlo (incluso si la orden está APPROVED)
            if (approvalUrl != null) {
                logger.debug("[PayPal] Approval URL found");
                return approvalUrl;
            }
            
            // Si no hay approval URL, verificar el estado
            String status = order.status();
            if ("APPROVED".equals(status)) {
                // Si está APPROVED pero no hay approval URL, la orden ya fue aprobada
                // pero puede que falte capturar. Esto se manejará en el servicio.
                logger.warn("[PayPal] Order is APPROVED but no approval URL available");
                throw new IllegalArgumentException("Order is already approved. Payment should be processed automatically.");
            }
            
            if (!"CREATED".equals(status) && !"SAVED".equals(status)) {
                logger.warn("[PayPal] Order is in status {}, cannot resume", status);
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
     * Usa transacción SERIALIZABLE para evitar condiciones de carrera cuando múltiples pagos se procesan simultáneamente
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    private void addCoinsToAccount(PaymentTransaction transaction) {
        // Usar CoinService para agregar monedas (esto enviará la notificación automáticamente)
        AccountMaster account = transaction.getAccount();
        if (account == null) {
            logger.error("[PayPal] Account is null for transaction: {}", transaction.getId());
            return;
        }
        
        if (transaction.getCoinPackage() == null || transaction.getCoinPackage().getId() == null) {
            logger.error("[PayPal] CoinPackage is null for transaction: {}", transaction.getId());
            return;
        }
        
        logger.info("[PayPal] Adding {} coins to account {} using CoinService", 
                   transaction.getCoinsAmount(), account.getId());
        
        // Usar CoinService que maneja notificaciones
        coinService.addCoinsToAccount(account.getId(), transaction.getCoinPackage().getId(), transaction);
    }
    
}

