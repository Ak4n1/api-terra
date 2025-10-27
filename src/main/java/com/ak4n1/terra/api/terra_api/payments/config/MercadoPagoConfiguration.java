package com.ak4n1.terra.api.terra_api.payments.config;

import com.mercadopago.MercadoPagoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuración de Mercado Pago
 */
@Configuration
public class MercadoPagoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoConfiguration.class);
    
    @Value("${mercadopago.access.token:}")
    private String accessToken;
    
    @Value("${mercadopago.public.key:}")
    private String publicKey;
    
    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;
    
    @Value("${mercadopago.notification.url:}")
    private String notificationUrl;
    
    @Value("${mercadopago.return.url:}")
    private String returnUrl;
    
    @Value("${mercadopago.cancel.url:}")
    private String cancelUrl;
    
    /**
     * Inicializar SDK de Mercado Pago
     * TODO: Descomentar cuando se descargue la dependencia de Mercado Pago
     */
    @PostConstruct
    public void initializeMercadoPago() {
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                MercadoPagoConfig.setAccessToken(accessToken);
                logger.info("Mercado Pago SDK inicializado correctamente");
                logger.info("Modo Sandbox: {}", sandboxEnabled);
                logger.info("Access Token configurado: {}", accessToken.substring(0, 10) + "...");
            } else {
                logger.warn("Access Token de Mercado Pago no configurado");
            }
        } catch (Exception e) {
            logger.error("Error al inicializar Mercado Pago SDK: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Bean para obtener la configuración de Mercado Pago
     */
    @Bean
    public MercadoPagoProperties mercadoPagoProperties() {
        return new MercadoPagoProperties(
            accessToken,
            publicKey,
            sandboxEnabled,
            notificationUrl,
            returnUrl,
            cancelUrl
        );
    }
    
    /**
     * Clase interna para propiedades de Mercado Pago
     */
    public static class MercadoPagoProperties {
        private final String accessToken;
        private final String publicKey;
        private final boolean sandboxEnabled;
        private final String notificationUrl;
        private final String returnUrl;
        private final String cancelUrl;
        
        public MercadoPagoProperties(String accessToken, String publicKey, boolean sandboxEnabled, 
                                   String notificationUrl, String returnUrl, String cancelUrl) {
            this.accessToken = accessToken;
            this.publicKey = publicKey;
            this.sandboxEnabled = sandboxEnabled;
            this.notificationUrl = notificationUrl;
            this.returnUrl = returnUrl;
            this.cancelUrl = cancelUrl;
        }
        
        // Getters
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
        
        public boolean isSandboxEnabled() {
            return sandboxEnabled;
        }
        
        public String getNotificationUrl() {
            return notificationUrl;
        }
        
        public String getReturnUrl() {
            return returnUrl;
        }
        
        public String getCancelUrl() {
            return cancelUrl;
        }
    }
}
