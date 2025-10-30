package com.ak4n1.terra.api.terra_api.security.services;

import com.ak4n1.terra.api.terra_api.security.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para rate limiting usando Bucket4j.
 * 
 * <p>Gestiona buckets de tasa de límite por IP y endpoint para prevenir
 * ataques de fuerza bruta y abuso de endpoints críticos.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
public class RateLimitService {

    // Map de buckets por IP
    private final Map<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();
    
    private final RateLimitConfig config;

    public RateLimitService(RateLimitConfig config) {
        this.config = config;
    }

    /**
     * Obtiene o crea un bucket para el endpoint de login.
     * 
     * @param ipAddress IP del cliente
     * @return Bucket configurado para login
     */
    public Bucket getLoginBucket(String ipAddress) {
        String key = "login:" + ipAddress;
        return bucketsByIp.computeIfAbsent(key, k -> createBucket(config.getLoginAttempts(), config.getLoginWindowMinutes()));
    }

    /**
     * Obtiene o crea un bucket para el endpoint de refresh.
     * 
     * @param ipAddress IP del cliente
     * @return Bucket configurado para refresh
     */
    public Bucket getRefreshBucket(String ipAddress) {
        String key = "refresh:" + ipAddress;
        return bucketsByIp.computeIfAbsent(key, k -> createBucket(config.getRefreshAttempts(), config.getRefreshWindowMinutes()));
    }

    /**
     * Obtiene o crea un bucket para el endpoint de registro.
     * 
     * @param ipAddress IP del cliente
     * @return Bucket configurado para registro
     */
    public Bucket getRegisterBucket(String ipAddress) {
        String key = "register:" + ipAddress;
        return bucketsByIp.computeIfAbsent(key, k -> createBucket(config.getRegisterAttempts(), config.getRegisterWindowMinutes()));
    }

    /**
     * Obtiene o crea un bucket para el endpoint de reset password.
     * 
     * @param ipAddress IP del cliente
     * @return Bucket configurado para reset password
     */
    public Bucket getResetPasswordBucket(String ipAddress) {
        String key = "reset:" + ipAddress;
        return bucketsByIp.computeIfAbsent(key, k -> createBucket(config.getResetPasswordAttempts(), config.getResetPasswordWindowMinutes()));
    }

    /**
     * Obtiene la IP real del cliente desde el request.
     * Considera headers de proxy como X-Forwarded-For.
     * 
     * @param request HttpServletRequest
     * @return IP del cliente
     */
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs (X-Forwarded-For), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * Crea un nuevo bucket con la configuración especificada.
     * 
     * @param capacity Capacidad (número de tokens)
     * @param refillPeriodMinutes Período de relleno en minutos
     * @return Bucket configurado
     */
    private Bucket createBucket(int capacity, int refillPeriodMinutes) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(refillPeriodMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Intenta consumir un token del bucket. Retorna true si hay tokens disponibles.
     * 
     * @param bucket Bucket a consultar
     * @return true si hay tokens disponibles, false si se alcanzó el límite
     */
    public boolean tryConsume(Bucket bucket) {
        return bucket.tryConsume(1);
    }

    /**
     * Obtiene los tokens restantes en el bucket.
     * 
     * @param bucket Bucket a consultar
     * @return Número de tokens disponibles
     */
    public long getAvailableTokens(Bucket bucket) {
        return bucket.getAvailableTokens();
    }
}

