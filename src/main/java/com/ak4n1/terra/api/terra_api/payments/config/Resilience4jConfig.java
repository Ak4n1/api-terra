package com.ak4n1.terra.api.terra_api.payments.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.core.IntervalFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de Resilience4j para retry logic en llamadas a APIs externas
 */
@Configuration
public class Resilience4jConfig {

    /**
     * Retry config para MercadoPago API calls
     * - Max 3 intentos
     * - Exponential backoff: 500ms, 1s, 2s
     * - Solo reintenta errores de conexión (5xx), no errores de validación (4xx)
     */
    @Bean(name = "mercadopagoRetry")
    public Retry mercadopagoRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 2))
                .retryOnException(throwable -> {
                    // Solo reintentar errores de conexión, no errores de validación
                    String className = throwable.getClass().getSimpleName();
                    String message = throwable.getMessage() != null ? 
                                   throwable.getMessage().toLowerCase() : "";
                    
                    // No reintentar errores de validación (4xx)
                    if (className.contains("MPApiException")) {
                        return !message.contains("401") && 
                               !message.contains("403") && 
                               !message.contains("404") && 
                               !message.contains("400");
                    }
                    
                    // Reintentar errores de conexión (MPException)
                    return className.contains("MPException");
                })
                .build();

        return RetryRegistry.of(config).retry("mercadopagoRetry", config);
    }

    /**
     * Retry config para PayPal API calls
     * - Max 3 intentos
     * - Exponential backoff: 500ms, 1s, 2s
     * - Solo reintenta errores de conexión (5xx), no errores de validación (4xx)
     */
    @Bean(name = "paypalRetry")
    public Retry paypalRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 2))
                .retryOnException(throwable -> {
                    // Solo reintentar errores de conexión, no errores de validación
                    String message = throwable.getMessage() != null ? 
                                   throwable.getMessage().toLowerCase() : "";
                    
                    // No reintentar errores de validación (4xx)
                    if (message.contains("401") || message.contains("403") || 
                        message.contains("404") || message.contains("400")) {
                        return false;
                    }
                    
                    // Reintentar errores de conexión y timeouts
                    return throwable instanceof java.io.IOException ||
                           throwable instanceof java.net.SocketTimeoutException ||
                           throwable instanceof java.util.concurrent.TimeoutException;
                })
                .build();

        return RetryRegistry.of(config).retry("paypalRetry", config);
    }
}

