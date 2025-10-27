package com.ak4n1.terra.api.terra_api.payments.dto;

/**
 * DTO de respuesta para preferencia de pago de Mercado Pago
 */
public class PaymentPreferenceResponse {
    
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private String publicKey;
    private String status;
    private String message;
    
    // Constructor por defecto
    public PaymentPreferenceResponse() {}
    
    // Constructor con parámetros
    public PaymentPreferenceResponse(String preferenceId, String initPoint, String sandboxInitPoint, String publicKey) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
        this.sandboxInitPoint = sandboxInitPoint;
        this.publicKey = publicKey;
        this.status = "success";
    }
    
    // Constructor de error
    public PaymentPreferenceResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters y Setters
    public String getPreferenceId() {
        return preferenceId;
    }
    
    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }
    
    public String getInitPoint() {
        return initPoint;
    }
    
    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }
    
    public String getSandboxInitPoint() {
        return sandboxInitPoint;
    }
    
    public void setSandboxInitPoint(String sandboxInitPoint) {
        this.sandboxInitPoint = sandboxInitPoint;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
