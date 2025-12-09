package com.ak4n1.terra.api.terra_api.payments.strategies;

import java.util.Map;

/**
 * Strategy interface for webhook handlers
 * Each payment provider has different webhook formats and verification methods
 */
public interface WebhookStrategy {
    
    /**
     * Get the provider name this webhook strategy handles
     */
    String getProviderName();
    
    /**
     * Verify webhook authenticity
     * @param headers Request headers
     * @param payload Webhook payload
     * @return true if webhook is authentic
     */
    boolean verifyWebhook(Map<String, String> headers, String payload) throws Exception;
    
    /**
     * Process the webhook payload
     * @param payload Webhook payload as JSON string
     * @return Result message
     */
    String processWebhook(String payload) throws Exception;
    
    /**
     * Check if this strategy supports the given provider
     */
    boolean supports(String provider);
}

