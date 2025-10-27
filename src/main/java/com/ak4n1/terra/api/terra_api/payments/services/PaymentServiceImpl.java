package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPackageResponseDTO;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPurchaseRequest;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.CoinPackageRepository;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio principal de pagos
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    @Autowired
    private CoinPackageRepository coinPackageRepository;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private MercadoPagoService mercadoPagoService;
    
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
            return List.of();
        }
    }
    
    @Override
    public CoinPackageResponseDTO getPackageById(Long packageId) {
        try {
            Optional<CoinPackage> packageOpt = coinPackageRepository.findByIdAndActiveTrue(packageId);
            if (packageOpt.isEmpty()) {
                logger.warn("Paquete no encontrado o inactivo: {}", packageId);
                return null;
            }
            
            return new CoinPackageResponseDTO(packageOpt.get());
        } catch (Exception e) {
            logger.error("Error al obtener paquete por ID {}: {}", packageId, e.getMessage(), e);
            return null;
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
            return List.of();
        }
    }
    
    @Override
    public PaymentPreferenceResponse createPaymentPreference(CoinPurchaseRequest request) {
        try {
            // Validar que el paquete existe y está activo
            Optional<CoinPackage> packageOpt = coinPackageRepository.findByIdAndActiveTrue(request.getPackageId());
            if (packageOpt.isEmpty()) {
                logger.error("Paquete no encontrado o inactivo: {}", request.getPackageId());
                return new PaymentPreferenceResponse("error", "Paquete no encontrado");
            }
            
            // Validar que la cuenta existe
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(request.getAccountId());
            if (accountOpt.isEmpty()) {
                logger.error("Cuenta no encontrada: {}", request.getAccountId());
                return new PaymentPreferenceResponse("error", "Cuenta no encontrada");
            }
            
            CoinPackage coinPackage = packageOpt.get();
            AccountMaster account = accountOpt.get();
            
            // Crear transacción pendiente
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setAccount(account);
            transaction.setCoinPackage(coinPackage);
            transaction.setAmount(coinPackage.getPrice());
            transaction.setCoinsAmount(coinPackage.getTotalCoins());
            transaction.setBaseCoins(coinPackage.getCoinsAmount());
            transaction.setBonusCoins(coinPackage.getBonusCoins());
            transaction.setReturnUrl(request.getReturnUrl());
            transaction.setCancelUrl(request.getCancelUrl());
            transaction.setNotificationUrl(request.getNotificationUrl());
            
            paymentTransactionRepository.save(transaction);
            
            // Crear preferencia en Mercado Pago
            PaymentPreferenceResponse preference = mercadoPagoService.createPaymentPreference(
                coinPackage, 
                request.getAccountId(), 
                request.getReturnUrl(), 
                request.getCancelUrl()
            );
            
            // Verificar si hubo error en la creación de la preferencia
            if ("error".equals(preference.getStatus())) {
                logger.error("Error al crear preferencia en Mercado Pago: {}", preference.getMessage());
                // Eliminar la transacción creada ya que falló
                paymentTransactionRepository.delete(transaction);
                return preference;
            }
            
            // Actualizar transacción con el ID de preferencia
            if (preference.getPreferenceId() != null) {
                transaction.setMpPreferenceId(preference.getPreferenceId());
                paymentTransactionRepository.save(transaction);
            }
            
            logger.info("Preferencia de pago creada exitosamente. Transacción: {}, Paquete: {}, Cuenta: {}", 
                       transaction.getId(), coinPackage.getId(), account.getId());
            
            return preference;
            
        } catch (Exception e) {
            logger.error("Error al crear preferencia de pago: {}", e.getMessage(), e);
            return new PaymentPreferenceResponse("error", "Error interno del servidor");
        }
    }
    
    @Override
    public boolean processMercadoPagoWebhook(String payload, String signature) {
        try {
            logger.info("Procesando webhook de Mercado Pago");
            
            // Procesar webhook con Mercado Pago
            boolean success = mercadoPagoService.processWebhook(payload, signature);
            
            if (success) {
                logger.info("Webhook procesado exitosamente");
            } else {
                logger.warn("Error al procesar webhook");
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error al procesar webhook: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<PaymentTransaction> getAccountTransactionHistory(Long accountId) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return List.of();
            }
            
            return paymentTransactionRepository.findByAccountOrderByCreatedAtDesc(accountOpt.get());
            
        } catch (Exception e) {
            logger.error("Error al obtener historial de transacciones de la cuenta {}: {}", accountId, e.getMessage(), e);
            return List.of();
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
            logger.error("Error al obtener estado de transacción {}: {}", transactionId, e.getMessage(), e);
            return "ERROR";
        }
    }
    
    @Override
    public boolean refundTransaction(Long transactionId, String reason) {
        try {
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                logger.error("Transacción no encontrada: {}", transactionId);
                return false;
            }
            
            PaymentTransaction transaction = transactionOpt.get();
            
            // Verificar que la transacción esté aprobada
            if (!transaction.isApproved()) {
                logger.warn("No se puede reembolsar una transacción no aprobada: {}", transactionId);
                return false;
            }
            
            // Intentar reembolsar en Mercado Pago
            boolean refundSuccess = mercadoPagoService.refundPayment(transaction.getMpPaymentId(), reason);
            
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
                
                logger.info("Reembolso procesado exitosamente. Transacción: {}, Razón: {}", transactionId, reason);
                return true;
            } else {
                logger.error("Error al procesar reembolso en Mercado Pago. Transacción: {}", transactionId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al reembolsar transacción {}: {}", transactionId, e.getMessage(), e);
            return false;
        }
    }
}
