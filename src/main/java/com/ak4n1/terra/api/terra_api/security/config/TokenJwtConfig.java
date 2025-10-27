package com.ak4n1.terra.api.terra_api.security.config;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class TokenJwtConfig {

    @Value("${jwt.secret}")
    private String secretString;
    
    public static SecretKey SECRET_KEY;

    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public static final String CONTENT_TYPE = "application/json";

    // Tiempos de expiración en milisegundos
    public static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L; // 2 horas en milisegundos
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 días
}
