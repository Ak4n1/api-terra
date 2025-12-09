package com.ak4n1.terra.api.terra_api.payments.strategies;

import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;

/**
 * Strategy interface for payment providers
 * Each payment provider (MercadoPago, PayPal, Stripe, etc.) implements this interface
 */
public interface PaymentStrategy {
    
    /**
     * Get the provider name (mercadopago, paypal, stripe, etc.)
     */
    String getProviderName();
    
    /**
     * Create a payment preference/order
     * @param transaction The payment transaction to process
     * @return Payment preference response with payment URL/ID
     */
    PaymentPreferenceResponse createPayment(PaymentTransaction transaction) throws Exception;
    
    /**
     * Verify and capture a payment
     * @param paymentId The payment ID from the provider
     * @return The updated transaction
     */
    PaymentTransaction capturePayment(String paymentId) throws Exception;
    
    /**
     * Refund a payment
     * @param paymentId The payment ID to refund
     * @return true if refund was successful
     */
    boolean refundPayment(String paymentId) throws Exception;
    
    /**
     * Check if this strategy supports the given provider name
     */
    boolean supports(String provider);
}

