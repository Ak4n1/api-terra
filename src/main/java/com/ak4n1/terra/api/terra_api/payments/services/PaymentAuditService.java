package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentAudit;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para registro de auditoría de cambios de monedas
 */
@Service
@Transactional
public class PaymentAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentAuditService.class);
    
    @Autowired
    private PaymentAuditRepository auditRepository;
    
    /**
     * Registrar cambio de saldo por compra
     */
    public void auditPurchase(AccountMaster account, Integer coinsBefore, Integer coinsAfter, 
                              PaymentTransaction transaction, String provider) {
        try {
            PaymentAudit audit = new PaymentAudit();
            audit.setAccount(account);
            audit.setTransactionType("PURCHASE");
            audit.setCoinsBefore(coinsBefore);
            audit.setCoinsAfter(coinsAfter);
            audit.setCoinsChanged(coinsAfter - coinsBefore);
            audit.setPaymentTransaction(transaction);
            audit.setPaymentProvider(provider);
            audit.setReason("Purchase: " + transaction.getCoinPackage().getName());
            
            auditRepository.save(audit);
            
            logger.info("[Audit] PURCHASE recorded: Account={}, Coins: {} -> {}, Provider={}", 
                       account.getId(), coinsBefore, coinsAfter, provider);
        } catch (Exception e) {
            logger.error("[Audit] Error recording purchase: {}", e.getMessage(), e);
            // No fallar la transacción principal si falla la auditoría
        }
    }
    
    /**
     * Registrar cambio de saldo por reembolso
     */
    public void auditRefund(AccountMaster account, Integer coinsBefore, Integer coinsAfter, 
                           PaymentTransaction transaction, String reason) {
        try {
            PaymentAudit audit = new PaymentAudit();
            audit.setAccount(account);
            audit.setTransactionType("REFUND");
            audit.setCoinsBefore(coinsBefore);
            audit.setCoinsAfter(coinsAfter);
            audit.setCoinsChanged(coinsAfter - coinsBefore);
            audit.setPaymentTransaction(transaction);
            audit.setPaymentProvider(transaction.getProvider());
            audit.setReason(reason);
            
            auditRepository.save(audit);
            
            logger.info("[Audit] REFUND recorded: Account={}, Coins: {} -> {}", 
                       account.getId(), coinsBefore, coinsAfter);
        } catch (Exception e) {
            logger.error("[Audit] Error recording refund: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Registrar cambio de saldo por ajuste manual de admin
     */
    public void auditAdminAdjustment(AccountMaster account, Integer coinsBefore, Integer coinsAfter, 
                                     String reason, String performedBy, String ipAddress) {
        try {
            PaymentAudit audit = new PaymentAudit();
            audit.setAccount(account);
            audit.setTransactionType(coinsAfter > coinsBefore ? "ADMIN_ADD" : "ADMIN_REMOVE");
            audit.setCoinsBefore(coinsBefore);
            audit.setCoinsAfter(coinsAfter);
            audit.setCoinsChanged(coinsAfter - coinsBefore);
            audit.setReason(reason);
            audit.setPerformedBy(performedBy);
            audit.setIpAddress(ipAddress);
            
            auditRepository.save(audit);
            
            logger.warn("[Audit] ADMIN_ADJUSTMENT recorded: Account={}, Coins: {} -> {}, By={}", 
                       account.getId(), coinsBefore, coinsAfter, performedBy);
        } catch (Exception e) {
            logger.error("[Audit] Error recording admin adjustment: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Registrar transferencia de monedas entre usuarios
     */
    public void auditTransfer(AccountMaster fromAccount, AccountMaster toAccount, 
                             Integer amount, String reason) {
        try {
            // Registro para quien envía
            PaymentAudit auditSend = new PaymentAudit();
            auditSend.setAccount(fromAccount);
            auditSend.setTransactionType("TRANSFER_SEND");
            auditSend.setCoinsBefore(fromAccount.getTerraCoins() + amount); // Antes de restar
            auditSend.setCoinsAfter(fromAccount.getTerraCoins());
            auditSend.setCoinsChanged(-amount);
            auditSend.setReason("Transfer to account " + toAccount.getId() + ": " + reason);
            auditRepository.save(auditSend);
            
            // Registro para quien recibe
            PaymentAudit auditReceive = new PaymentAudit();
            auditReceive.setAccount(toAccount);
            auditReceive.setTransactionType("TRANSFER_RECEIVE");
            auditReceive.setCoinsBefore(toAccount.getTerraCoins() - amount); // Antes de sumar
            auditReceive.setCoinsAfter(toAccount.getTerraCoins());
            auditReceive.setCoinsChanged(amount);
            auditReceive.setReason("Transfer from account " + fromAccount.getId() + ": " + reason);
            auditRepository.save(auditReceive);
            
            logger.info("[Audit] TRANSFER recorded: From={} To={}, Amount={}", 
                       fromAccount.getId(), toAccount.getId(), amount);
        } catch (Exception e) {
            logger.error("[Audit] Error recording transfer: {}", e.getMessage(), e);
        }
    }
}

