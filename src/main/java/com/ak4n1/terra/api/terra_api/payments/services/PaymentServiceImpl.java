package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPackageResponseDTO;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPurchaseRequest;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentTransactionDTO;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.CoinPackageRepository;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.exceptions.PackageNotFoundException;
import com.ak4n1.terra.api.terra_api.payments.exceptions.PaymentException;
import com.ak4n1.terra.api.terra_api.payments.factory.PaymentStrategyFactory;
import com.ak4n1.terra.api.terra_api.payments.strategies.MercadoPagoStrategy;
import com.ak4n1.terra.api.terra_api.payments.strategies.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;

/**
 * Implementación del servicio principal de pagos - Refactorizado con Factory Pattern
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("1000000.00");
    
    @Autowired
    private CoinPackageRepository coinPackageRepository;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;
    
    @Autowired
    private CoinService coinService;
    
    @Override
    public List<CoinPackageResponseDTO> getAllActivePackages() {
        try {
            List<CoinPackage> packages = coinPackageRepository.findActivePackagesOrdered();
            return packages.stream()
                    .map(CoinPackageResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener paquetes activos: {}", e.getMessage(), e);
            throw new PaymentException("Failed to retrieve active packages", e);
        }
    }
    
    @Override
    public List<CoinPackageResponseDTO> getActivePackagesByCurrency(String currency) {
        try {
            // Validate currency
            List<String> ALLOWED_CURRENCIES = List.of("USD", "ARS");
            if (!ALLOWED_CURRENCIES.contains(currency.toUpperCase())) {
                throw new IllegalArgumentException("Invalid currency: " + currency);
            }
            
            List<CoinPackage> packages = coinPackageRepository.findByActiveTrueAndCurrencyOrderBySortOrderAsc(currency.toUpperCase());
            logger.info("Found {} active packages for currency {}", packages.size(), currency);
            return packages.stream()
                    .map(CoinPackageResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener paquetes activos por moneda {}: {}", currency, e.getMessage(), e);
            throw new PaymentException("Failed to retrieve packages for currency: " + currency, e);
        }
    }
    
    @Override
    public CoinPackageResponseDTO getPackageById(Long packageId) {
        try {
            Optional<CoinPackage> packageOpt = coinPackageRepository.findByIdAndActiveTrue(packageId);
            if (packageOpt.isEmpty()) {
                throw new PackageNotFoundException("Package not found or inactive: " + packageId);
            }
            
            return new CoinPackageResponseDTO(packageOpt.get());
        } catch (PackageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener paquete por ID {}: {}", packageId, e.getMessage(), e);
            throw new PaymentException("Failed to retrieve package: " + packageId, e);
        }
    }
    
    @Override
    public List<CoinPackageResponseDTO> getPopularPackages() {
        try {
            List<CoinPackage> packages = coinPackageRepository.findByPopularTrueAndActiveTrue();
            return packages.stream()
                    .map(CoinPackageResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener paquetes populares: {}", e.getMessage(), e);
            throw new PaymentException("Failed to retrieve popular packages", e);
        }
    }
    
    @Override
    public PaymentPreferenceResponse createPaymentPreference(CoinPurchaseRequest request) {
        try {
            // Determinar el proveedor (por defecto MercadoPago)
            String provider = request.getProvider() != null ? request.getProvider() : "mercadopago";
            
            // SECURITY: Whitelist de proveedores permitidos
            List<String> ALLOWED_PROVIDERS = List.of("mercadopago", "paypal");
            if (!ALLOWED_PROVIDERS.contains(provider.toLowerCase())) {
                logger.error("Invalid payment provider attempted: {}", provider);
                return new PaymentPreferenceResponse("error", "Invalid payment provider");
            }
            
            logger.info("[Payment] Creating payment preference for provider: {}", provider);
            
            // Validar que el paquete existe y está activo
            Optional<CoinPackage> packageOpt = coinPackageRepository.findByIdAndActiveTrue(request.getPackageId());
            if (packageOpt.isEmpty()) {
                throw new PackageNotFoundException("Package not found or inactive: " + request.getPackageId());
            }
            
            // Validar que la cuenta existe
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(request.getAccountId());
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Account not found: " + request.getAccountId());
            }
            
            CoinPackage coinPackage = packageOpt.get();
            AccountMaster account = accountOpt.get();
            
            // SECURITY: Verificar que el email esté verificado
            if (!account.isEmailVerified()) {
                logger.error("Email not verified for account: {}", account.getId());
                return new PaymentPreferenceResponse("error", "Email verification required to make purchases");
            }
            
            // SECURITY: Validar que el precio del paquete sea válido
            BigDecimal packagePrice = coinPackage.getPrice();
            if (packagePrice.compareTo(BigDecimal.ZERO) <= 0 || packagePrice.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
                logger.error("Invalid payment amount detected! Package ID: {}, Amount: {}", 
                           coinPackage.getId(), packagePrice);
                throw new IllegalArgumentException("Invalid payment amount");
            }
            
            // Crear transacción pendiente
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setProvider(provider);
            transaction.setAccount(account);
            transaction.setCoinPackage(coinPackage);
            transaction.setAmount(coinPackage.getPrice());
            transaction.setCoinsAmount(coinPackage.getTotalCoins());
            transaction.setBaseCoins(coinPackage.getCoinsAmount());
            transaction.setBonusCoins(coinPackage.getBonusCoins());
            transaction.setReturnUrl(request.getReturnUrl());
            transaction.setCancelUrl(request.getCancelUrl());
            transaction.setNotificationUrl(request.getNotificationUrl());
            transaction.setIpAddress(request.getIpAddress()); // IP del cliente
            transaction.setPaymentMethod(provider); // mercadopago, paypal
            transaction.setPaymentType("digital_currency"); // Tipo genérico para coins
            
            // SECURITY: Validar que el monto de la transacción coincida con el paquete
            if (!transaction.getAmount().equals(coinPackage.getPrice())) {
                logger.error("Price mismatch detected! Transaction: {}, Package: {}", 
                           transaction.getAmount(), coinPackage.getPrice());
                throw new SecurityException("Price mismatch detected");
            }
            
            paymentTransactionRepository.save(transaction);
            
            // Obtener la estrategia de pago correspondiente
            PaymentStrategy strategy = paymentStrategyFactory.getPaymentStrategy(provider);
            
            // Crear el pago usando la estrategia
            PaymentPreferenceResponse preference = strategy.createPayment(transaction);
            
            logger.info("[Payment] Payment preference created successfully");
            return preference;
            
        } catch (IllegalArgumentException | PackageNotFoundException | IllegalStateException | SecurityException e) {
            throw e; // Re-throw para que PaymentExceptionHandler lo maneje
        } catch (Exception e) {
            logger.error("Error al crear preferencia de pago: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment preference", e);
        }
    }
    
    @Override
    public boolean processMercadoPagoWebhook(String payload, String signature) {
        try {
            // Mantener este método por retrocompatibilidad
            // El WebhookController usará directamente el Factory
            logger.info("[Payment] Processing webhook (legacy method)");
            return true;
        } catch (Exception e) {
            logger.error("Error al procesar webhook: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransactionDTO> getAccountTransactionHistory(Long accountId) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Account not found: " + accountId);
            }
            
            // JOIN FETCH carga las relaciones lazy dentro de la transacción
            List<PaymentTransaction> transactions = paymentTransactionRepository.findByAccountOrderByCreatedAtDesc(accountOpt.get());
            
            // Convertir a DTOs dentro de la transacción (antes de que se cierre la sesión)
            // Esto evita LazyInitializationException
            return transactions.stream()
                    .map(transaction -> {
                        try {
                            return new PaymentTransactionDTO(transaction);
                        } catch (Exception e) {
                            logger.error("Error converting transaction {} to DTO: {}", transaction.getId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener historial de transacciones de la cuenta {}: {}", accountId, e.getMessage(), e);
            throw new PaymentException("Failed to retrieve transaction history for account: " + accountId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDTO> getAccountTransactionHistoryPaginated(Long accountId, int page, int size) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Account not found: " + accountId);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<PaymentTransaction> transactionPage = paymentTransactionRepository.findByAccountOrderByCreatedAtDesc(accountOpt.get(), pageable);

            List<PaymentTransactionDTO> dtos = transactionPage.getContent().stream()
                    .map(transaction -> {
                        try {
                            if (transaction.getCoinPackage() != null) {
                                Hibernate.initialize(transaction.getCoinPackage());
                            }
                            return new PaymentTransactionDTO(transaction);
                        } catch (Exception e) {
                            logger.error("Error converting transaction {} to DTO: {}", transaction.getId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            return new PageImpl<>(dtos, pageable, transactionPage.getTotalElements());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener historial paginado de transacciones de la cuenta {}: {}", accountId, e.getMessage(), e);
            throw new PaymentException("Failed to retrieve paginated transaction history for account: " + accountId, e);
        }
    }
    
    @Override
    public CoinService.CoinAccountStats getAccountPaymentStats(Long accountId) {
        return coinService.getAccountStats(accountId);
    }
    
    @Override
    public String getTransactionStatus(Long transactionId) {
        try {
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                return "NOT_FOUND";
            }
            
            return transactionOpt.get().getStatus().name();
            
        } catch (Exception e) {
            logger.error("Error al obtener estado de transaccion {}: {}", transactionId, e.getMessage(), e);
            return "ERROR";
        }
    }
    
    @Override
    public boolean refundTransaction(Long transactionId, String reason) {
        try {
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                logger.error("Transaccion no encontrada: {}", transactionId);
                return false;
            }
            
            PaymentTransaction transaction = transactionOpt.get();
            
            // Verificar que la transacción esté aprobada
            if (!transaction.isApproved()) {
                logger.warn("No se puede reembolsar una transaccion no aprobada: {}", transactionId);
                return false;
            }
            
            // Obtener la estrategia de pago correspondiente
            String provider = transaction.getProvider() != null ? transaction.getProvider() : "mercadopago";
            PaymentStrategy strategy = paymentStrategyFactory.getPaymentStrategy(provider);
            
            // Obtener el payment ID según el proveedor
            String paymentId = null;
            if ("mercadopago".equalsIgnoreCase(provider)) {
                paymentId = transaction.getMpPaymentId();
            } else if ("paypal".equalsIgnoreCase(provider)) {
                paymentId = transaction.getPaypalOrderId();
            }
            
            if (paymentId == null) {
                logger.error("No payment ID found for transaction: {}", transactionId);
                return false;
            }
            
            // Intentar reembolsar usando la estrategia
            boolean refundSuccess = strategy.refundPayment(paymentId);
            
            if (refundSuccess) {
                // Actualizar estado de la transacción
                transaction.setStatus(com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus.REFUNDED);
                paymentTransactionRepository.save(transaction);
                
                // Remover monedas de la cuenta
                coinService.removeCoinsFromAccount(
                    transaction.getAccount().getId(), 
                    transaction.getCoinsAmount(), 
                    "Reembolso: " + reason
                );
                
                return true;
            } else {
                logger.error("Error al procesar reembolso. Transaccion: {}", transactionId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al reembolsar transaccion {}: {}", transactionId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getResumePaymentUrl(Long transactionId, Long accountId) {
        try {
            // Buscar la transacción
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                throw new IllegalArgumentException("Transaction not found");
            }
            
            PaymentTransaction transaction = transactionOpt.get();
            
            // Validar que la transacción pertenece al usuario
            if (!transaction.getAccount().getId().equals(accountId)) {
                throw new IllegalArgumentException("Transaction does not belong to this account");
            }
            
            // Validar que la transacción esté pendiente
            if (!transaction.isPending()) {
                throw new IllegalArgumentException("Transaction is not pending. Status: " + transaction.getStatus());
            }
            
            String provider = transaction.getProvider() != null ? transaction.getProvider() : "mercadopago";
            
            // Generar URL según el provider
            if ("mercadopago".equalsIgnoreCase(provider)) {
                String preferenceId = transaction.getMpPreferenceId();
                if (preferenceId == null || preferenceId.isEmpty()) {
                    throw new IllegalArgumentException("MercadoPago preference ID not found for this transaction");
                }
                
                // CRITICAL: Verificar el estado real en Mercado Pago antes de generar la URL
                // Esto previene que se genere una URL para una transacción ya pagada
                try {
                    PaymentStrategy strategy = paymentStrategyFactory.getPaymentStrategy(provider);
                    if (strategy instanceof MercadoPagoStrategy) {
                        MercadoPagoStrategy mpStrategy = (MercadoPagoStrategy) strategy;
                        
                        // Buscar si hay un paymentId asociado a esta transacción
                        String paymentId = transaction.getMpPaymentId();
                        if (paymentId != null && !paymentId.isEmpty()) {
                            // Si hay paymentId, verificar el estado del pago en Mercado Pago
                            PaymentTransaction updatedTransaction = mpStrategy.capturePayment(paymentId);
                            
                            // Si el pago ya fue aprobado, no permitir reanudar
                            if (updatedTransaction.getStatus() == PaymentStatus.APPROVED) {
                                throw new IllegalArgumentException("This payment has already been completed. Please refresh the page.");
                            }
                        }
                    }
                } catch (Exception e) {
                    // Si falla la verificación, loguear pero continuar (puede ser un error temporal)
                    logger.warn("Could not verify payment status in Mercado Pago for transaction {}: {}", transactionId, e.getMessage());
                    // Continuar con la generación de URL (mejor intentar que bloquear)
                }
                
                // Construir URL de MercadoPago
                // El dominio depende del país, pero podemos usar el genérico o construir según preferencia
                // Para simplificar, usamos el dominio genérico que funciona para todos los países
                return "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preferenceId;
                
            } else if ("paypal".equalsIgnoreCase(provider)) {
                // Para PayPal, las órdenes expiran después de cierto tiempo
                // Si la orden expiró, necesitaríamos crear una nueva
                // Por ahora, retornamos un mensaje indicando que debe crear una nueva orden
                throw new IllegalArgumentException("PayPal orders cannot be resumed. Please create a new payment.");
                
            } else {
                throw new IllegalArgumentException("Unsupported payment provider: " + provider);
            }
            
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw para que el controller maneje el error apropiadamente
        } catch (Exception e) {
            logger.error("Error al generar URL de reanudacion para transaccion {}: {}", transactionId, e.getMessage(), e);
            throw new RuntimeException("Error generating resume payment URL: " + e.getMessage());
        }
    }
}
