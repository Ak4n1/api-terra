package com.ak4n1.terra.api.terra_api.payments.dto;

import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO para respuestas de transacciones de pago (historial)
 */
public class PaymentTransactionDTO {
    
    private Long id;
    private String externalUuid;
    private BigDecimal amount;
    private Integer coinsAmount;
    private Integer baseCoins;
    private Integer bonusCoins;
    private PaymentStatus status;
    private String provider;
    private String mpPreferenceId;
    private String paypalOrderId;
    private Date createdAt;
    private Date updatedAt;
    private Date processedAt;
    
    // Información del paquete (sin relación lazy)
    private Long packageId;
    private String packageName;
    private String packageCurrency;
    
    // Constructor por defecto
    public PaymentTransactionDTO() {}
    
    // Constructor desde entidad
    public PaymentTransactionDTO(PaymentTransaction transaction) {
        this.id = transaction.getId();
        this.externalUuid = transaction.getExternalUuid();
        this.amount = transaction.getAmount();
        this.coinsAmount = transaction.getCoinsAmount();
        this.baseCoins = transaction.getBaseCoins();
        this.bonusCoins = transaction.getBonusCoins();
        this.status = transaction.getStatus();
        this.provider = transaction.getProvider();
        this.mpPreferenceId = transaction.getMpPreferenceId();
        this.paypalOrderId = transaction.getPaypalOrderId();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        this.processedAt = transaction.getProcessedAt();
        
        // Obtener información del paquete de forma segura
        if (transaction.getCoinPackage() != null) {
            this.packageId = transaction.getCoinPackage().getId();
            this.packageName = transaction.getCoinPackage().getName();
            this.packageCurrency = transaction.getCoinPackage().getCurrency();
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getExternalUuid() {
        return externalUuid;
    }
    
    public void setExternalUuid(String externalUuid) {
        this.externalUuid = externalUuid;
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
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getMpPreferenceId() {
        return mpPreferenceId;
    }
    
    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }
    
    public String getPaypalOrderId() {
        return paypalOrderId;
    }
    
    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
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
    
    public Long getPackageId() {
        return packageId;
    }
    
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getPackageCurrency() {
        return packageCurrency;
    }
    
    public void setPackageCurrency(String packageCurrency) {
        this.packageCurrency = packageCurrency;
    }
}

