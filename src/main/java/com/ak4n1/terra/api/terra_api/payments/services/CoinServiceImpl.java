package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
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

/**
 * Implementación del servicio de gestión de monedas
 */
@Service
@Transactional
public class CoinServiceImpl implements CoinService {
    
    private static final Logger logger = LoggerFactory.getLogger(CoinServiceImpl.class);
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private CoinPackageRepository coinPackageRepository;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Override
    public void addCoinsToAccount(Long accountId, Long packageId, PaymentTransaction transaction) {
        try {
            // Buscar la cuenta
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                logger.error("Cuenta no encontrada: {}", accountId);
                throw new RuntimeException("Cuenta no encontrada");
            }
            
            AccountMaster account = accountOpt.get();
            
            // Buscar el paquete
            Optional<CoinPackage> packageOpt = coinPackageRepository.findById(packageId);
            if (packageOpt.isEmpty()) {
                logger.error("Paquete no encontrado: {}", packageId);
                throw new RuntimeException("Paquete no encontrado");
            }
            
            CoinPackage coinPackage = packageOpt.get();
            
            // Calcular total de monedas (base + bonus)
            int totalCoins = coinPackage.getTotalCoins();
            
            // Agregar monedas a la cuenta
            int currentCoins = account.getTerraCoins() != null ? account.getTerraCoins() : 0;
            account.setTerraCoins(currentCoins + totalCoins);
            
            // Guardar la cuenta
            accountMasterRepository.save(account);
            
            // Actualizar la transacción
            transaction.setStatus(com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus.APPROVED);
            transaction.setCoinsAmount(totalCoins);
            transaction.setBaseCoins(coinPackage.getCoinsAmount());
            transaction.setBonusCoins(coinPackage.getBonusCoins());
            transaction.markAsProcessed();
            
            paymentTransactionRepository.save(transaction);
            
            logger.info("Monedas agregadas exitosamente. Cuenta: {}, Paquete: {}, Monedas: {} (Base: {}, Bonus: {})", 
                       accountId, packageId, totalCoins, coinPackage.getCoinsAmount(), coinPackage.getBonusCoins());
            
        } catch (Exception e) {
            logger.error("Error al agregar monedas a la cuenta {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error al procesar el pago", e);
        }
    }
    
    @Override
    public void addCoinsToAccount(Long accountId, Integer coinsAmount, String reason) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Cuenta no encontrada");
            }
            
            AccountMaster account = accountOpt.get();
            int currentCoins = account.getTerraCoins() != null ? account.getTerraCoins() : 0;
            account.setTerraCoins(currentCoins + coinsAmount);
            
            accountMasterRepository.save(account);
            
            logger.info("Monedas agregadas manualmente. Cuenta: {}, Cantidad: {}, Razón: {}", 
                       accountId, coinsAmount, reason);
            
        } catch (Exception e) {
            logger.error("Error al agregar monedas manualmente a la cuenta {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error al agregar monedas", e);
        }
    }
    
    @Override
    public void removeCoinsFromAccount(Long accountId, Integer coinsAmount, String reason) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Cuenta no encontrada");
            }
            
            AccountMaster account = accountOpt.get();
            int currentCoins = account.getTerraCoins() != null ? account.getTerraCoins() : 0;
            
            if (currentCoins < coinsAmount) {
                throw new RuntimeException("Saldo insuficiente");
            }
            
            account.setTerraCoins(currentCoins - coinsAmount);
            accountMasterRepository.save(account);
            
            logger.info("Monedas removidas. Cuenta: {}, Cantidad: {}, Razón: {}", 
                       accountId, coinsAmount, reason);
            
        } catch (Exception e) {
            logger.error("Error al remover monedas de la cuenta {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error al remover monedas", e);
        }
    }
    
    @Override
    public Integer getAccountCoinsBalance(Long accountId) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return 0;
            }
            
            return accountOpt.get().getTerraCoins() != null ? accountOpt.get().getTerraCoins() : 0;
            
        } catch (Exception e) {
            logger.error("Error al obtener balance de monedas de la cuenta {}: {}", accountId, e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean hasEnoughCoins(Long accountId, Integer requiredCoins) {
        Integer balance = getAccountCoinsBalance(accountId);
        return balance >= requiredCoins;
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
    public CoinAccountStats getAccountStats(Long accountId) {
        try {
            Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return new CoinAccountStats(0, 0, 0, 0);
            }
            
            AccountMaster account = accountOpt.get();
            Integer currentBalance = account.getTerraCoins() != null ? account.getTerraCoins() : 0;
            
            // Obtener estadísticas de transacciones
            Object[] stats = paymentTransactionRepository.getTransactionStatsByAccount(account);
            
            Integer totalTransactions = stats[0] != null ? ((Long) stats[0]).intValue() : 0;
            Integer totalEarned = stats[1] != null ? ((Long) stats[1]).intValue() : 0;
            Integer totalSpent = 0; // Por ahora no implementamos gastos
            
            return new CoinAccountStats(currentBalance, totalEarned, totalSpent, totalTransactions);
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de la cuenta {}: {}", accountId, e.getMessage(), e);
            return new CoinAccountStats(0, 0, 0, 0);
        }
    }
}
