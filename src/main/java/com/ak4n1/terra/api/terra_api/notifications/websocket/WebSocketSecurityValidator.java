package com.ak4n1.terra.api.terra_api.notifications.websocket;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validador de seguridad para conexiones WebSocket.
 * 
 * <p>Valida tokens JWT, aplica rate limiting y valida orígenes permitidos.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class WebSocketSecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSecurityValidator.class);

    private final ActiveTokenRepository tokenRepository;
    private final AccountMasterRepository accountMasterRepository;

    // Rate limiting: IP -> Lista de timestamps de intentos
    private final Map<String, List<Long>> connectionAttemptsByIp = new ConcurrentHashMap<>();
    
    // Rate limiting: Email -> Número de conexiones activas
    private final Map<String, Integer> activeConnectionsByUser = new ConcurrentHashMap<>();

    // Configuración de rate limiting
    private static final int MAX_CONNECTIONS_PER_IP_PER_MINUTE = 5;
    private static final int MAX_ACTIVE_CONNECTIONS_PER_USER = 3;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000; // 1 minuto

    // Orígenes permitidos
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "https://l2terra.online",
            "http://localhost:4200",
            "file://"
    );

    public WebSocketSecurityValidator(ActiveTokenRepository tokenRepository,
                                      AccountMasterRepository accountMasterRepository) {
        this.tokenRepository = tokenRepository;
        this.accountMasterRepository = accountMasterRepository;
    }

    /**
     * Valida el token JWT y retorna el usuario autenticado.
     * 
     * @param token el token JWT
     * @return el usuario autenticado
     * @throws SecurityException si el token es inválido o el usuario no está habilitado
     */
    public AccountMaster validateToken(String token) throws SecurityException {
        if (token == null || token.isBlank()) {
            throw new SecurityException("Token is required");
        }

        try {
            // Validar JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(TokenJwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Verificar en BD
            Optional<ActiveToken> activeTokenOpt = tokenRepository.findByToken(token);
            if (activeTokenOpt.isEmpty()) {
                throw new SecurityException("Token not found in database");
            }

            ActiveToken activeToken = activeTokenOpt.get();

            // Validar expiración
            if (activeToken.getExpiresAt().before(new Date())) {
                tokenRepository.delete(activeToken);
                throw new SecurityException("Token expired");
            }

            // Validar usuario
            AccountMaster user = activeToken.getAccountMaster();
            if (user == null) {
                throw new SecurityException("User not found");
            }

            if (!user.isEnabled()) {
                throw new SecurityException("User is disabled");
            }

            if (!user.isEmailVerified()) {
                throw new SecurityException("Email not verified");
            }

            return user;

        } catch (JwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            throw new SecurityException("Invalid token");
        }
    }

    /**
     * Valida el origen de la petición.
     * 
     * @param origin el origen de la petición
     * @return true si el origen está permitido
     */
    public boolean validateOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }

        // Verificar si el origen está en la lista permitida
        return ALLOWED_ORIGINS.stream()
                .anyMatch(allowed -> origin.equals(allowed) || origin.startsWith(allowed));
    }

    /**
     * Valida rate limiting por IP.
     * 
     * @param ipAddress la dirección IP
     * @return true si se permite la conexión
     */
    public boolean validateRateLimitByIp(String ipAddress) {
        long now = System.currentTimeMillis();
        List<Long> attempts = connectionAttemptsByIp.computeIfAbsent(ipAddress, k -> new ArrayList<>());

        // Limpiar intentos antiguos (fuera de la ventana)
        attempts.removeIf(timestamp -> (now - timestamp) > RATE_LIMIT_WINDOW_MS);

        // Verificar límite
        if (attempts.size() >= MAX_CONNECTIONS_PER_IP_PER_MINUTE) {
            logger.warn("Rate limit exceeded for IP: {}", ipAddress);
            return false;
        }

        // Registrar intento
        attempts.add(now);
        return true;
    }

    /**
     * Valida rate limiting por usuario (conexiones simultáneas).
     * 
     * @param userEmail el email del usuario
     * @return true si se permite la conexión
     */
    public boolean validateRateLimitByUser(String userEmail) {
        Integer activeConnections = activeConnectionsByUser.getOrDefault(userEmail, 0);
        
        if (activeConnections >= MAX_ACTIVE_CONNECTIONS_PER_USER) {
            logger.warn("Max active connections exceeded for user: {}", userEmail);
            return false;
        }

        return true;
    }

    /**
     * Incrementa el contador de conexiones activas para un usuario.
     * 
     * @param userEmail el email del usuario
     */
    public void incrementActiveConnections(String userEmail) {
        activeConnectionsByUser.merge(userEmail, 1, Integer::sum);
    }

    /**
     * Decrementa el contador de conexiones activas para un usuario.
     * 
     * @param userEmail el email del usuario
     */
    public void decrementActiveConnections(String userEmail) {
        activeConnectionsByUser.computeIfPresent(userEmail, (k, v) -> {
            int newValue = v - 1;
            return newValue > 0 ? newValue : null;
        });
    }

    /**
     * Limpia entradas expiradas de rate limiting (llamar periódicamente).
     */
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        
        // Limpiar intentos de IP antiguos
        connectionAttemptsByIp.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(timestamp -> (now - timestamp) > RATE_LIMIT_WINDOW_MS);
            return entry.getValue().isEmpty();
        });
    }
}

