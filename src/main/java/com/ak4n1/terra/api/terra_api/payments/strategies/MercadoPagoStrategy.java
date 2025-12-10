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
import com.mercadopago.MercadoPagoConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
 * Implementación de la estrategia de pago para Mercado Pago.
 * 
 * <p>Esta clase maneja la creación de preferencias de pago, verificación de pagos
 * y actualización de estados de transacciones usando la API de Mercado Pago.
 * 
 * <p>Características principales:
 * <ul>
 *   <li>Creación de preferencias de pago con expiración de 24 horas</li>
 *   <li>Verificación de estado de pagos mediante la API de Mercado Pago</li>
 *   <li>Manejo de retry automático con Resilience4j para llamadas a la API</li>
 *   <li>Transacciones ACID con aislamiento SERIALIZABLE para prevenir race conditions</li>
 * </ul>
 * 
 * @author ak4n1
 * @since 3.0
 * @see PaymentStrategy
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
    
    @Autowired
    private RetryRegistry retryRegistry;
    
    @Autowired
    private CoinService coinService;
    
    /**
     * Obtiene el nombre del proveedor de pago.
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
     * Crea una preferencia de pago en Mercado Pago.
     * 
     * <p>Este método crea una preferencia de pago en Mercado Pago con los siguientes detalles:
     * <ul>
     *   <li>Item basado en el paquete de monedas solicitado</li>
     *   <li>URL de notificación para webhooks</li>
     *   <li>Expiración de 24 horas</li>
     *   <li>External reference con ID de cuenta y paquete</li>
     * </ul>
     * 
     * <p>La creación de la preferencia utiliza retry logic con exponential backoff
     * para manejar fallos temporales de la API de Mercado Pago.
     * 
     * @param transaction La transacción de pago con los detalles del paquete y cuenta
     * @return Respuesta con el ID de preferencia y URLs de pago (init_point y sandbox_init_point)
     * @throws Exception Si ocurre un error al crear la preferencia o si la API de Mercado Pago falla
     */
    @Override
    public PaymentPreferenceResponse createPayment(PaymentTransaction transaction) throws Exception {
        logger.debug("[MP] Creando preferencia de pago");
        
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
            
            // Crear preferencia en Mercado Pago con retry
            logger.info("[MP] Creando preferencia en Mercado Pago. Paquete: {}, Monto: {}", 
                       coinPackage.getName(), coinPackage.getPrice());
            Retry retry = retryRegistry.retry("mercadopagoRetry");
            Preference preference = Retry.decorateSupplier(retry, () -> {
                try {
                    return client.create(preferenceRequest);
                } catch (MPApiException e) {
                    throw new RuntimeException(e); // Envolver para Resilience4j
                } catch (MPException e) {
                    throw new RuntimeException(e); // Envolver para Resilience4j
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get();
            logger.info("[MP] ✅ Preferencia creada exitosamente. Preference ID: {}", preference.getId());
            
            // Actualizar transacción con preferenceId
            transaction.setMpPreferenceId(preference.getId());
            paymentTransactionRepository.save(transaction);
            logger.debug("[MP] Transaccion actualizada con preferenceId");
            
            // Retornar respuesta
            return new PaymentPreferenceResponse(
                preference.getId(),
                preference.getInitPoint(),
                preference.getSandboxInitPoint(),
                publicKey
            );
            
        } catch (RuntimeException e) {
            // Desenvolver excepciones envueltas por Resilience4j
            Throwable cause = e.getCause();
            if (cause instanceof MPApiException) {
                MPApiException mpApiEx = (MPApiException) cause;
                logger.error("[MP] Error de API (después de reintentos): {} - {}", 
                           mpApiEx.getApiResponse() != null ? mpApiEx.getApiResponse().getContent() : "N/A", 
                           mpApiEx.getMessage());
                throw new Exception("Error al crear preferencia en MercadoPago: " + mpApiEx.getMessage());
            } else if (cause instanceof MPException) {
                MPException mpEx = (MPException) cause;
                logger.error("[MP] Error de conexion (despues de reintentos): {}", mpEx.getMessage());
                throw new Exception("Error de conexión con MercadoPago: " + mpEx.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Verifica y obtiene el estado de un pago en Mercado Pago.
     * 
     * <p>Mercado Pago no requiere captura explícita ya que los webhooks manejan
     * automáticamente la actualización de estados. Este método se utiliza principalmente
     * para verificar el estado actual de un pago y actualizar la transacción local.
     * 
     * <p>El método utiliza retry logic para manejar fallos temporales de la API.
     * 
     * @param paymentId El ID del pago en Mercado Pago
     * @return La transacción actualizada con el estado más reciente
     * @throws Exception Si el pago no se encuentra o si ocurre un error al consultar la API
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public PaymentTransaction capturePayment(String paymentId) throws Exception {
        logger.debug("[MP] Capturando pago");
        
        // MercadoPago no requiere captura explícita, el webhook maneja todo
        // Este método se usa para verificar el estado
        try {
            logger.info("[MP] Verificando estado de pago. Payment ID: {}", paymentId);
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
            
            logger.info("[MP] Pago obtenido de Mercado Pago. Estado: {}, Monto: {}, Currency: {}", 
                       payment.getStatus(), payment.getTransactionAmount(), payment.getCurrencyId());
            
            PaymentTransaction transaction = paymentTransactionRepository.findByMpPaymentId(paymentId)
                    .orElseThrow(() -> new Exception("Transacción no encontrada para paymentId: " + paymentId));
            
            logger.info("[MP] Transaccion encontrada. Estado actual: {}", transaction.getStatus());
            
            // SECURITY: Validar que el paquete siga activo
            if (!transaction.getCoinPackage().isActive()) {
                logger.error("[MP] ❌ Package is no longer active for transaction");
                throw new IllegalStateException("Package is no longer available");
            }
            
            // Actualizar estado según Mercado Pago
            updateTransactionStatus(transaction, payment);
            logger.info("[MP] Estado de transaccion actualizado a: {}", transaction.getStatus());
            
            return transaction;
        } catch (RuntimeException e) {
            // Desenvolver excepciones envueltas por Resilience4j
            Throwable cause = e.getCause();
            if (cause instanceof MPApiException) {
                MPApiException mpApiEx = (MPApiException) cause;
                logger.error("[MP] Error de API (después de reintentos): {} - {}", 
                           mpApiEx.getApiResponse() != null ? mpApiEx.getApiResponse().getContent() : "N/A", 
                           mpApiEx.getMessage());
                throw new Exception("Error al obtener pago de MercadoPago: " + mpApiEx.getMessage(), mpApiEx);
            } else if (cause instanceof MPException) {
                MPException mpEx = (MPException) cause;
                logger.error("[MP] Error de conexion (despues de reintentos): {}", mpEx.getMessage());
                throw new Exception("Error de conexión con MercadoPago: " + mpEx.getMessage(), mpEx);
            }
            throw e;
        } catch (Exception e) {
            logger.error("[MP] Error capturando pago: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Reembolsa un pago en Mercado Pago.
     * 
     * <p><b>NOTA:</b> Esta funcionalidad aún no está implementada.
     * 
     * @param paymentId El ID del pago a reembolsar
     * @return false ya que la funcionalidad no está implementada
     * @throws Exception Si ocurre un error
     * @see <a href="https://www.mercadopago.com.ar/developers/es/reference/chargebacks/_payments_id_refunds/post">
     *      Documentación de reembolsos de Mercado Pago</a>
     */
    @Override
    public boolean refundPayment(String paymentId) throws Exception {
        logger.debug("[MP] Reembolsando pago");
        
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
     * Actualiza el estado de una transacción según el estado del pago en Mercado Pago.
     * 
     * <p>Este método mapea los estados de Mercado Pago a los estados internos del sistema
     * y procesa la acreditación de monedas cuando el pago es aprobado.
     * 
     * <p>Estados mapeados:
     * <ul>
     *   <li>approved → APPROVED (acredita monedas)</li>
     *   <li>rejected → REJECTED</li>
     *   <li>pending → PENDING</li>
     *   <li>in_process → IN_PROCESS</li>
     *   <li>cancelled → CANCELLED</li>
     * </ul>
     * 
     * <p><b>Validaciones de seguridad:</b>
     * <ul>
     *   <li>Previene procesar transacciones ya reembolsadas</li>
     *   <li>Previene procesar transacciones canceladas</li>
     *   <li>Evita acreditar monedas múltiples veces</li>
     * </ul>
     * 
     * @param transaction La transacción local a actualizar
     * @param payment El objeto Payment de Mercado Pago con el estado actual
     */
    private void updateTransactionStatus(PaymentTransaction transaction, Payment payment) {
        String status = payment.getStatus();
        logger.info("[MP] Actualizando estado. Estado de MP: {}, Estado actual de transaccion: {}", 
                   status, transaction.getStatus());
        
        switch (status) {
            case "approved":
                // Validar que la transacción no esté en un estado inválido
                if (PaymentStatus.REFUNDED.equals(transaction.getStatus())) {
                    logger.warn("[MP] ⚠️ Intento de procesar transaccion reembolsada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process refunded transaction");
                }
                
                if (PaymentStatus.CANCELLED.equals(transaction.getStatus())) {
                    logger.warn("[MP] ⚠️ Intento de procesar transaccion cancelada: {}", transaction.getId());
                    throw new IllegalStateException("Cannot process cancelled transaction");
                }
                
                if (!PaymentStatus.APPROVED.equals(transaction.getStatus())) {
                    logger.info("[MP] ✅ Pago aprobado. Procesando acreditacion de monedas");
                    transaction.setStatus(PaymentStatus.APPROVED);
                    addCoinsToAccount(transaction);
                } else {
                    logger.debug("[MP] Pago ya estaba aprobado, omitiendo");
                }
                break;
            case "rejected":
                logger.info("[MP] ❌ Pago rechazado por Mercado Pago");
                transaction.setStatus(PaymentStatus.REJECTED);
                break;
            case "pending":
                logger.info("[MP] ⏳ Pago pendiente");
                transaction.setStatus(PaymentStatus.PENDING);
                break;
            case "in_process":
                logger.info("[MP] 🔄 Pago en proceso");
                transaction.setStatus(PaymentStatus.IN_PROCESS);
                break;
            case "cancelled":
                logger.info("[MP] 🚫 Pago cancelado");
                transaction.setStatus(PaymentStatus.CANCELLED);
                break;
            default:
                logger.warn("[MP] ⚠️ Estado desconocido de Mercado Pago: {}", status);
                break;
        }
        
        transaction.setMpPaymentId(payment.getId().toString());
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        logger.debug("[MP] Transaccion guardada con nuevo estado");
    }
    
    /**
     * Agrega monedas a la cuenta del usuario después de un pago aprobado.
     * 
     * <p><b>CRÍTICO:</b> Este método utiliza aislamiento SERIALIZABLE para prevenir
     * condiciones de carrera cuando múltiples pagos se procesan simultáneamente.
     * 
     * <p>El proceso incluye:
     * <ol>
     *   <li>Obtener el saldo actual de monedas de la cuenta</li>
     *   <li>Calcular el nuevo saldo (actual + monedas del paquete)</li>
     *   <li>Actualizar el saldo en la base de datos</li>
     *   <li>Registrar la operación en auditoría</li>
     * </ol>
     * 
     * <p>Si cualquier paso falla, toda la transacción se revierte automáticamente
     * (rollback) para mantener la integridad de los datos.
     * 
     * @param transaction La transacción de pago aprobada con las monedas a acreditar
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    private void addCoinsToAccount(PaymentTransaction transaction) {
        // Usar CoinService para agregar monedas (esto enviará la notificación automáticamente)
        AccountMaster account = transaction.getAccount();
        if (account == null) {
            logger.error("[MP] Account is null for transaction: {}", transaction.getId());
            return;
        }
        
        if (transaction.getCoinPackage() == null || transaction.getCoinPackage().getId() == null) {
            logger.error("[MP] CoinPackage is null for transaction: {}", transaction.getId());
            return;
        }
        
        logger.info("[MP] Agregando {} monedas a cuenta {} usando CoinService", 
                   transaction.getCoinsAmount(), account.getId());
        
        // Usar CoinService que maneja notificaciones
        coinService.addCoinsToAccount(account.getId(), transaction.getCoinPackage().getId(), transaction);
    }
}

