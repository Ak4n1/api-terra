package com.ak4n1.terra.api.terra_api.payments.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository para transacciones de pago
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    /**
     * Buscar transacción por ID de pago de Mercado Pago
     */
    Optional<PaymentTransaction> findByMpPaymentId(String mpPaymentId);
    
    /**
     * Buscar transacción por ID de preferencia de Mercado Pago
     */
    Optional<PaymentTransaction> findByMpPreferenceId(String mpPreferenceId);
    
    /**
     * Buscar transacción por external reference
     */
    Optional<PaymentTransaction> findByExternalReference(String externalReference);
    
    /**
     * Buscar transacciones por cuenta
     */
    List<PaymentTransaction> findByAccountOrderByCreatedAtDesc(AccountMaster account);
    
    /**
     * Buscar transacciones por cuenta con paginación
     */
    Page<PaymentTransaction> findByAccountOrderByCreatedAtDesc(AccountMaster account, Pageable pageable);
    
    /**
     * Buscar transacciones por estado
     */
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    
    /**
     * Buscar transacciones por cuenta y estado
     */
    List<PaymentTransaction> findByAccountAndStatusOrderByCreatedAtDesc(AccountMaster account, PaymentStatus status);
    
    /**
     * Buscar transacciones aprobadas por cuenta
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.account = :account AND pt.status = 'APPROVED' ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findApprovedTransactionsByAccount(@Param("account") AccountMaster account);
    
    /**
     * Contar transacciones por cuenta y estado
     */
    long countByAccountAndStatus(AccountMaster account, PaymentStatus status);
    
    /**
     * Buscar transacciones por rango de fechas
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Buscar transacciones por cuenta y rango de fechas
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.account = :account AND pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByAccountAndDateRange(@Param("account") AccountMaster account, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Obtener estadísticas de transacciones por cuenta
     */
    @Query("SELECT COUNT(pt), SUM(pt.coinsAmount), SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.account = :account AND pt.status = 'APPROVED'")
    Object[] getTransactionStatsByAccount(@Param("account") AccountMaster account);
    
    /**
     * Buscar transacciones pendientes
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.createdAt < :expirationDate")
    List<PaymentTransaction> findExpiredPendingTransactions(@Param("expirationDate") Date expirationDate);
}
