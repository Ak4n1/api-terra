package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;

/**
 * Servicio para integración con Mercado Pago
 * 
 * TODO: Implementar métodos cuando se descargue la dependencia
 */
public interface MercadoPagoService {
    
    /**
     * Crear preferencia de pago en Mercado Pago
     */
    PaymentPreferenceResponse createPaymentPreference(CoinPackage coinPackage, Long accountId, String returnUrl, String cancelUrl);
    
    /**
     * Procesar webhook de Mercado Pago
     */
    boolean processWebhook(String payload, String signature);
    
    /**
     * Obtener información de un pago
     */
    Object getPaymentInfo(String paymentId);
    
    /**
     * Verificar estado de un pago
     */
    String getPaymentStatus(String paymentId);
    
    /**
     * Reembolsar un pago
     */
    boolean refundPayment(String paymentId, String reason);
}
