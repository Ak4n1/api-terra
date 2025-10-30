package com.ak4n1.terra.api.terra_api.security.filters;

import com.ak4n1.terra.api.terra_api.security.services.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Filtro de rate limiting para endpoints críticos de autenticación.
 * 
 * <p>Este filtro intercepta las peticiones a endpoints de auth y aplica
 * límites de tasa por IP para prevenir ataques de fuerza bruta.
 * 
 * @see RateLimitService
 * @author ak4n1
 * @since 1.0
 */
@Component
@Order(1) // Ejecutar antes de otros filtros de seguridad
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired
    private RateLimitService rateLimitService;

    /**
     * Paths que requieren rate limiting.
     */
    private static final String[] RATE_LIMITED_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/register",
            "/api/auth/resend-reset-email",
            "/api/auth/reset-password"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Solo aplicar rate limiting a paths específicos
        if (!isRateLimitedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = rateLimitService.getClientIp(request);
        Bucket bucket = getBucketForPath(path, clientIp);

        if (rateLimitService.tryConsume(bucket)) {
            // Hay tokens disponibles, continuar
            long remaining = rateLimitService.getAvailableTokens(bucket);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            filterChain.doFilter(request, response);
        } else {
            // Límite alcanzado, rechazar petición
            logger.warn("🚫 [RATE LIMIT] Límite alcanzado para IP: {} en path: {}", clientIp, path);
            sendRateLimitError(response, path);
        }
    }

    /**
     * Obtiene el bucket adecuado según el path de la petición.
     * 
     * @param path Path de la petición
     * @param clientIp IP del cliente
     * @return Bucket configurado
     */
    private Bucket getBucketForPath(String path, String clientIp) {
        if (path.equals("/api/auth/login")) {
            return rateLimitService.getLoginBucket(clientIp);
        } else if (path.equals("/api/auth/refresh")) {
            return rateLimitService.getRefreshBucket(clientIp);
        } else if (path.equals("/api/auth/register")) {
            return rateLimitService.getRegisterBucket(clientIp);
        } else if (path.equals("/api/auth/resend-reset-email") || path.equals("/api/auth/reset-password")) {
            return rateLimitService.getResetPasswordBucket(clientIp);
        }
        // Por defecto, usar bucket de login
        return rateLimitService.getLoginBucket(clientIp);
    }

    /**
     * Verifica si un path requiere rate limiting.
     * 
     * @param path Path a verificar
     * @return true si requiere rate limiting
     */
    private boolean isRateLimitedPath(String path) {
        for (String limitedPath : RATE_LIMITED_PATHS) {
            if (path.equals(limitedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Envía una respuesta de error cuando se alcanza el límite de tasa.
     * 
     * @param response HttpServletResponse
     * @param path Path que causó el límite
     * @throws IOException si hay error escribiendo la respuesta
     */
    private void sendRateLimitError(HttpServletResponse response, String path) throws IOException {
            response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.setHeader("Retry-After", "900"); // 15 minutos en segundos
        
        Map<String, Object> error = Map.of(
                "message", "Too many requests. Please try again later.",
                "error", "RATE_LIMIT_EXCEEDED",
                "path", path
        );
        
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}

