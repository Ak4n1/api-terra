package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;

/**
 * Servicio para gestión de monedas
 */
public interface CoinService {
    
    /**
     * Agregar monedas a una cuenta después de un pago exitoso
     */
    void addCoinsToAccount(Long accountId, Long packageId, PaymentTransaction transaction);
    
    /**
     * Agregar monedas directamente (para administradores)
     */
    void addCoinsToAccount(Long accountId, Integer coinsAmount, String reason);
    
    /**
     * Remover monedas de una cuenta
     */
    void removeCoinsFromAccount(Long accountId, Integer coinsAmount, String reason);
    
    /**
     * Obtener balance de monedas de una cuenta
     */
    Integer getAccountCoinsBalance(Long accountId);
    
    /**
     * Verificar si una cuenta tiene suficientes monedas
     */
    boolean hasEnoughCoins(Long accountId, Integer requiredCoins);
    
    /**
     * Obtener historial de transacciones de monedas
     */
    java.util.List<PaymentTransaction> getAccountTransactionHistory(Long accountId);
    
    /**
     * Obtener estadísticas de monedas de una cuenta
     */
    CoinAccountStats getAccountStats(Long accountId);
    
    /**
     * Clase para estadísticas de cuenta
     */
    class CoinAccountStats {
        private Integer currentBalance;
        private Integer totalEarned;
        private Integer totalSpent;
        private Integer totalTransactions;
        
        public CoinAccountStats(Integer currentBalance, Integer totalEarned, Integer totalSpent, Integer totalTransactions) {
            this.currentBalance = currentBalance;
            this.totalEarned = totalEarned;
            this.totalSpent = totalSpent;
            this.totalTransactions = totalTransactions;
        }
        
        // Getters
        public Integer getCurrentBalance() {
            return currentBalance;
        }
        
        public Integer getTotalEarned() {
            return totalEarned;
        }
        
        public Integer getTotalSpent() {
            return totalSpent;
        }
        
        public Integer getTotalTransactions() {
            return totalTransactions;
        }
    }
}
