package com.ak4n1.terra.api.terra_api.payments.entities;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entidad para registrar transacciones de pago
 */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountMaster account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private CoinPackage coinPackage;
    
    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId; // ID de Mercado Pago
    
    @Column(name = "mp_preference_id")
    private String mpPreferenceId; // ID de preferencia de MP
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Monto pagado
    
    @Column(name = "coins_amount", nullable = false)
    private Integer coinsAmount; // Total de monedas (base + bonus)
    
    @Column(name = "base_coins", nullable = false)
    private Integer baseCoins; // Monedas base del paquete
    
    @Column(name = "bonus_coins", nullable = false)
    private Integer bonusCoins; // Monedas bonus
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "payment_method")
    private String paymentMethod; // "credit_card", "debit_card", "transfer", etc.
    
    @Column(name = "payment_type")
    private String paymentType; // "credit_card", "debit_card", "bank_transfer", etc.
    
    @Column(name = "installments")
    private Integer installments; // Cuotas
    
    @Column(name = "installment_amount")
    private BigDecimal installmentAmount; // Monto por cuota
    
    @Column(name = "mp_status")
    private String mpStatus; // Estado de Mercado Pago
    
    @Column(name = "mp_status_detail")
    private String mpStatusDetail; // Detalle del estado de MP
    
    @Column(name = "external_reference")
    private String externalReference; // Referencia externa
    
    @Column(name = "notification_url")
    private String notificationUrl; // URL de notificación
    
    @Column(name = "return_url")
    private String returnUrl; // URL de retorno
    
    @Column(name = "cancel_url")
    private String cancelUrl; // URL de cancelación
    
    @Column(name = "ip_address")
    private String ipAddress; // IP del cliente
    
    @Column(name = "user_agent")
    private String userAgent; // User agent del cliente
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @Column(name = "processed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedAt; // Cuando se procesó el pago
    
    // Constructor
    public PaymentTransaction() {
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
    
    public CoinPackage getCoinPackage() {
        return coinPackage;
    }
    
    public void setCoinPackage(CoinPackage coinPackage) {
        this.coinPackage = coinPackage;
    }
    
    public String getMpPaymentId() {
        return mpPaymentId;
    }
    
    public void setMpPaymentId(String mpPaymentId) {
        this.mpPaymentId = mpPaymentId;
    }
    
    public String getMpPreferenceId() {
        return mpPreferenceId;
    }
    
    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getCoinsAmount() {
        return coinsAmount;
    }
    
    public void setCoinsAmount(Integer coinsAmount) {
        this.coinsAmount = coinsAmount;
    }
    
    public Integer getBaseCoins() {
        return baseCoins;
    }
    
    public void setBaseCoins(Integer baseCoins) {
        this.baseCoins = baseCoins;
    }
    
    public Integer getBonusCoins() {
        return bonusCoins;
    }
    
    public void setBonusCoins(Integer bonusCoins) {
        this.bonusCoins = bonusCoins;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public Integer getInstallments() {
        return installments;
    }
    
    public void setInstallments(Integer installments) {
        this.installments = installments;
    }
    
    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }
    
    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }
    
    public String getMpStatus() {
        return mpStatus;
    }
    
    public void setMpStatus(String mpStatus) {
        this.mpStatus = mpStatus;
    }
    
    public String getMpStatusDetail() {
        return mpStatusDetail;
    }
    
    public void setMpStatusDetail(String mpStatusDetail) {
        this.mpStatusDetail = mpStatusDetail;
    }
    
    public String getExternalReference() {
        return externalReference;
    }
    
    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
    
    public String getNotificationUrl() {
        return notificationUrl;
    }
    
    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Date getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
    
    // Método para marcar como procesado
    public void markAsProcessed() {
        this.processedAt = new Date();
    }
    
    // Método para verificar si está aprobado
    public boolean isApproved() {
        return PaymentStatus.APPROVED.equals(this.status);
    }
    
    // Método para verificar si está pendiente
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }
}
