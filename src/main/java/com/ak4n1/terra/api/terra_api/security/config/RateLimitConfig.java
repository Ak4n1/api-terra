package com.ak4n1.terra.api.terra_api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Configuración para rate limiting de endpoints de autenticación.
 * 
 * <p>Esta clase contiene la configuración de límites de tasa por endpoint.
 * Los valores se cargan desde rate-limit.properties.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
@PropertySource("classpath:rate-limit.properties")
public class RateLimitConfig {

    // ============ LOGIN ============
    @Value("${rate.limit.login.attempts:5}")
    private int loginAttempts;
    
    @Value("${rate.limit.login.window.minutes:15}")
    private int loginWindowMinutes;

    // ============ REFRESH TOKEN ============
    @Value("${rate.limit.refresh.attempts:10}")
    private int refreshAttempts;
    
    @Value("${rate.limit.refresh.window.minutes:15}")
    private int refreshWindowMinutes;

    // ============ REGISTER ============
    @Value("${rate.limit.register.attempts:3}")
    private int registerAttempts;
    
    @Value("${rate.limit.register.window.minutes:60}")
    private int registerWindowMinutes;

    // ============ RESET PASSWORD ============
    @Value("${rate.limit.reset.password.attempts:3}")
    private int resetPasswordAttempts;
    
    @Value("${rate.limit.reset.password.window.minutes:60}")
    private int resetPasswordWindowMinutes;

    // Getters
    public int getLoginAttempts() {
        return loginAttempts;
    }

    public int getLoginWindowMinutes() {
        return loginWindowMinutes;
    }

    public int getRefreshAttempts() {
        return refreshAttempts;
    }

    public int getRefreshWindowMinutes() {
        return refreshWindowMinutes;
    }

    public int getRegisterAttempts() {
        return registerAttempts;
    }

    public int getRegisterWindowMinutes() {
        return registerWindowMinutes;
    }

    public int getResetPasswordAttempts() {
        return resetPasswordAttempts;
    }

    public int getResetPasswordWindowMinutes() {
        return resetPasswordWindowMinutes;
    }
}

