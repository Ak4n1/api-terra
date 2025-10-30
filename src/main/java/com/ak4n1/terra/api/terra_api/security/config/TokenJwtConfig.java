package com.ak4n1.terra.api.terra_api.security.config;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * Configuración para tokens JWT.
 * 
 * <p>Esta clase contiene la configuración estática para la generación y validación
 * de tokens JWT, incluyendo la clave secreta, tiempos de expiración y constantes.
 * Es la clase RECOMENDADA para acceder a la configuración de JWT.
 * 
 * @see io.jsonwebtoken.Jwts
 * @see com.ak4n1.terra.api.terra_api.security.filters.JwtAuthenticationFilter
 * @see com.ak4n1.terra.api.terra_api.security.filters.JwtValidationFilter
 * @author ak4n1
 * @since 1.0
 */
@Component
public class TokenJwtConfig {

    @Value("${jwt.secret}")
    private String secretString;
    
    private final Environment environment;
    
    /**
     * Flag estático que indica si las cookies deben tener el atributo Secure.
     * Solo true en producción (cuando SSL está habilitado o profile es 'prod').
     */
    public static boolean USE_SECURE_COOKIES = false;
    
    public TokenJwtConfig(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Clave secreta estática para firmar y validar tokens JWT.
     * Se inicializa automáticamente al iniciar la aplicación.
     */
    public static SecretKey SECRET_KEY;

    /**
     * Inicializa la clave secreta desde la configuración al iniciar Spring Boot.
     * 
     * <p>Convierte el string secreto desde application.properties en una SecretKey
     * usando el algoritmo HMAC SHA. También configura si se deben usar cookies Secure.
     */
    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        
        // Determinar si usar cookies Secure: solo en producción (perfil 'prod' o SSL habilitado)
        boolean isProd = environment.matchesProfiles("prod");
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        USE_SECURE_COOKIES = isProd || sslEnabled;
    }

    /**
     * Tipo de contenido para respuestas JSON.
     */
    public static final String CONTENT_TYPE = "application/json";

    /**
     * Tiempo de expiración del access token en milisegundos.
     * Valor: 2 horas (7,200,000 ms)
     */
    public static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L; // 2 horas en milisegundos
    
    /**
     * Tiempo de expiración del refresh token en milisegundos.
     * Valor: 7 días (604,800,000 ms)
     */
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 días
}
