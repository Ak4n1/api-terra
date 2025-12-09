package com.ak4n1.terra.api.terra_api.payments.entities;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * Entidad de auditoría para cambios de saldo de monedas
 * Registra TODOS los cambios (compras, reembolsos, ajustes admin, etc.)
 */
@Entity
@Table(name = "payment_audit")
public class PaymentAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountMaster account;
    
    @NotNull(message = "Transaction type is required")
    @Column(name = "transaction_type", length = 50, nullable = false)
    private String transactionType; // 'PURCHASE', 'REFUND', 'ADMIN_ADD', 'ADMIN_REMOVE', 'TRANSFER_SEND', 'TRANSFER_RECEIVE'
    
    @NotNull(message = "Coins before is required")
    @Column(name = "coins_before", nullable = false)
    private Integer coinsBefore;
    
    @NotNull(message = "Coins after is required")
    @Column(name = "coins_after", nullable = false)
    private Integer coinsAfter;
    
    @NotNull(message = "Coins changed is required")
    @Column(name = "coins_changed", nullable = false)
    private Integer coinsChanged; // Positivo = suma, Negativo = resta
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id")
    private PaymentTransaction paymentTransaction; // Null si no es por pago
    
    @Column(name = "payment_provider", length = 20)
    private String paymentProvider; // 'paypal', 'mercadopago', null si no aplica
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // Razón del cambio
    
    @Column(name = "performed_by")
    private String performedBy; // Email del admin si fue manual, null si automático
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON con info adicional
    
    // Constructor
    public PaymentAudit() {
        this.createdAt = new Date();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AccountMaster getAccount() {
        return account;
    }
    
    public void setAccount(AccountMaster account) {
        this.account = account;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public Integer getCoinsBefore() {
        return coinsBefore;
    }
    
    public void setCoinsBefore(Integer coinsBefore) {
        this.coinsBefore = coinsBefore;
    }
    
    public Integer getCoinsAfter() {
        return coinsAfter;
    }
    
    public void setCoinsAfter(Integer coinsAfter) {
        this.coinsAfter = coinsAfter;
    }
    
    public Integer getCoinsChanged() {
        return coinsChanged;
    }
    
    public void setCoinsChanged(Integer coinsChanged) {
        this.coinsChanged = coinsChanged;
    }
    
    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }
    
    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }
    
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}

