package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Servicio para limpieza automática de transacciones pendientes
 */
@Service
public class PaymentCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentCleanupService.class);
    
    @Autowired
    private PaymentTransactionRepository transactionRepository;
    
    /**
     * Limpiar transacciones pendientes expiradas
     * Se ejecuta cada hora
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    @Transactional
    public void cleanupExpiredTransactions() {
        try {
            // Transacciones pendientes de más de 24 horas
            Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            
            List<PaymentTransaction> expired = transactionRepository.findExpiredPendingTransactions(oneDayAgo);
            
            if (!expired.isEmpty()) {
                logger.info("[Cleanup] Found {} expired pending transactions", expired.size());
                
                for (PaymentTransaction transaction : expired) {
                    transaction.setStatus(PaymentStatus.CANCELLED);
                    transaction.setUpdatedAt(new Date());
                    transactionRepository.save(transaction);
                    
                    logger.info("[Cleanup] Transaction {} marked as CANCELLED (expired)", transaction.getId());
                }
                
                logger.info("[Cleanup] Successfully cleaned up {} expired transactions", expired.size());
            }
            
        } catch (Exception e) {
            logger.error("[Cleanup] Error cleaning expired transactions: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpiar manualmente transacciones expiradas (para admin)
     */
    @Transactional
    public int manualCleanup() {
        Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        List<PaymentTransaction> expired = transactionRepository.findExpiredPendingTransactions(oneDayAgo);
        
        for (PaymentTransaction transaction : expired) {
            transaction.setStatus(PaymentStatus.CANCELLED);
            transaction.setUpdatedAt(new Date());
            transactionRepository.save(transaction);
        }
        
        logger.info("[Cleanup] Manual cleanup: {} transactions", expired.size());
        return expired.size();
    }
}

