package com.ak4n1.terra.api.terra_api.payments.factory;

import com.ak4n1.terra.api.terra_api.payments.strategies.PaymentStrategy;
import com.ak4n1.terra.api.terra_api.payments.strategies.WebhookStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Factory for creating payment strategy instances
 * Automatically discovers all registered strategies via Spring dependency injection
 */
@Component
public class PaymentStrategyFactory {
    
    private final List<PaymentStrategy> paymentStrategies;
    private final List<WebhookStrategy> webhookStrategies;
    
    public PaymentStrategyFactory(List<PaymentStrategy> paymentStrategies, 
                                  List<WebhookStrategy> webhookStrategies) {
        this.paymentStrategies = paymentStrategies;
        this.webhookStrategies = webhookStrategies;
    }
    
    /**
     * Get payment strategy for the given provider
     * @param provider Provider name (mercadopago, paypal, stripe, etc.)
     * @return Payment strategy
     * @throws IllegalArgumentException if provider is not supported
     */
    public PaymentStrategy getPaymentStrategy(String provider) {
        Optional<PaymentStrategy> strategy = paymentStrategies.stream()
                .filter(s -> s.supports(provider.toLowerCase()))
                .findFirst();
        
        return strategy.orElseThrow(() -> 
            new IllegalArgumentException("Unsupported payment provider: " + provider));
    }
    
    /**
     * Get webhook strategy for the given provider
     * @param provider Provider name (mercadopago, paypal, stripe, etc.)
     * @return Webhook strategy
     * @throws IllegalArgumentException if provider is not supported
     */
    public WebhookStrategy getWebhookStrategy(String provider) {
        Optional<WebhookStrategy> strategy = webhookStrategies.stream()
                .filter(s -> s.supports(provider.toLowerCase()))
                .findFirst();
        
        return strategy.orElseThrow(() -> 
            new IllegalArgumentException("Unsupported webhook provider: " + provider));
    }
    
    /**
     * Get all supported payment providers
     */
    public List<String> getSupportedProviders() {
        return paymentStrategies.stream()
                .map(PaymentStrategy::getProviderName)
                .toList();
    }
}

