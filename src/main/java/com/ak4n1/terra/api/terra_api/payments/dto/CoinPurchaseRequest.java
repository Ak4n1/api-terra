package com.ak4n1.terra.api.terra_api.payments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de request para compra de monedas
 */
public class CoinPurchaseRequest {
    
    @NotNull(message = "El ID del paquete es obligatorio")
    @Min(value = 1, message = "El ID del paquete debe ser válido")
    private Long packageId;
    
    // El accountId ya no es obligatorio, se obtendrá del usuario autenticado
    private Long accountId;
    
    // Provider de pago (mercadopago, paypal, stripe, etc.) - default: mercadopago
    @Pattern(regexp = "^(mercadopago|paypal)$", message = "Invalid payment provider. Allowed: mercadopago, paypal")
    private String provider = "mercadopago";
    
    private String returnUrl;
    private String cancelUrl;
    private String notificationUrl;
    private String ipAddress; // IP del cliente que hace la compra
    
    // Constructor por defecto
    public CoinPurchaseRequest() {}
    
    // Constructor con parámetros
    public CoinPurchaseRequest(Long packageId, Long accountId, String returnUrl, String cancelUrl) {
        this.packageId = packageId;
        this.accountId = accountId;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }
    
    // Getters y Setters
    public Long getPackageId() {
        return packageId;
    }
    
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
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
    
    public String getNotificationUrl() {
        return notificationUrl;
    }
    
    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
