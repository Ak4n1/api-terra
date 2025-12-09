package com.ak4n1.terra.api.terra_api.payments.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository para auditoría de cambios de saldo
 */
@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, Long> {
    
    /**
     * Buscar auditorías por cuenta
     */
    List<PaymentAudit> findByAccountOrderByCreatedAtDesc(AccountMaster account);
    
    /**
     * Buscar auditorías por cuenta con paginación
     */
    Page<PaymentAudit> findByAccountOrderByCreatedAtDesc(AccountMaster account, Pageable pageable);
    
    /**
     * Buscar auditorías por tipo de transacción
     */
    List<PaymentAudit> findByTransactionTypeOrderByCreatedAtDesc(String transactionType);
    
    /**
     * Buscar auditorías por proveedor
     */
    List<PaymentAudit> findByPaymentProviderOrderByCreatedAtDesc(String paymentProvider);
    
    /**
     * Buscar auditorías por rango de fechas
     */
    @Query("SELECT pa FROM PaymentAudit pa WHERE pa.createdAt BETWEEN :startDate AND :endDate ORDER BY pa.createdAt DESC")
    List<PaymentAudit> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Buscar auditorías por cuenta y rango de fechas
     */
    @Query("SELECT pa FROM PaymentAudit pa WHERE pa.account = :account AND pa.createdAt BETWEEN :startDate AND :endDate ORDER BY pa.createdAt DESC")
    List<PaymentAudit> findByAccountAndDateRange(@Param("account") AccountMaster account, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Obtener suma de cambios por cuenta
     */
    @Query("SELECT SUM(pa.coinsChanged) FROM PaymentAudit pa WHERE pa.account = :account")
    Long getTotalCoinsChangedByAccount(@Param("account") AccountMaster account);
}

