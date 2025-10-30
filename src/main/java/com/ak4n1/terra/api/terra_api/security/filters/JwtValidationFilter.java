package com.ak4n1.terra.api.terra_api.security.filters;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filtro de validación JWT que verifica tokens en peticiones autenticadas.
 * 
 * <p>Este filtro intercepta las peticiones (excepto rutas públicas), extrae el token
 * de las cookies, valida su existencia en BD, verifica su expiración y carga el contexto
 * de seguridad de Spring. Es ejecutado después de JwtAuthenticationFilter.
 * 
 * @see BasicAuthenticationFilter
 * @see com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig
 * @see com.ak4n1.terra.api.terra_api.security.filters.JwtAuthenticationFilter
 * @author ak4n1
 * @since 1.0
 */
public class JwtValidationFilter extends BasicAuthenticationFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);
    private final ActiveTokenRepository tokenRepo;

    /**
     * Lista de rutas que no requieren validación de token (rutas públicas).
     */
    private static final List<String> excludedPaths = List.of(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/reset-password",
            "/api/auth/resend-reset-email",
            "/api/auth/register",
            "/api/auth/resend-verification",
            "/api/auth/verify-email",
            "/api/auth/google/login",
            "/api/auth/refresh",
            "/api/kick/channels",
            "/api/game/ranking/top-pvp",
            "/api/game/ranking/top-pk",
            "/api/game/ranking/top-pk",
            "/api/game/ranking/top-clans",
            "/api/game/patch-notes",
            "/api/stats",
            "/api/payments/webhook",
            "/api/payments/webhook/**"


    );


    /**
     * Constructor que inicializa el filtro con las dependencias necesarias.
     * 
     * @param authManager Gestor de autenticación
     * @param tokenRepo Repositorio de tokens activos para validación
     */
    public JwtValidationFilter(AuthenticationManager authManager,
                               ActiveTokenRepository tokenRepo) {
        super(authManager);
        this.tokenRepo = tokenRepo;
    }

    /**
     * Determina si el filtro debe procesar esta petición o no.
     * 
     * <p>Excluye rutas públicas y peticiones OPTIONS (preflight de CORS).
     * 
     * @param request HttpServletRequest a evaluar
     * @return true si el filtro NO debe procesar la petición, false si debe procesarla
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Siempre dejar pasar OPTIONS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        boolean excluded = excludedPaths.stream().anyMatch(path::startsWith);


        return excluded;
    }

    /**
     * Valida el token JWT y establece el contexto de seguridad de Spring.
     * 
     * <p>Extrae el token de las cookies, verifica su existencia y validez en BD,
     * valida la expiración, parsea los claims y establece la autenticación en el
     * SecurityContextHolder.
     * 
     * @param req HttpServletRequest con el token en cookie
     * @param res HttpServletResponse para enviar errores si es necesario
     * @param chain FilterChain para continuar el procesamiento
     * @throws IOException si hay error procesando la petición
     * @throws ServletException si hay error en el servlet
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String token = obtenerTokenDeCookie(req);
        String deviceId = req.getHeader("X-Device-Id");

        if (token == null || token.isBlank()) {
            logger.warn("❌ [JWT] No se recibió token.");
            sendError(res, HttpServletResponse.SC_FORBIDDEN, "No token provided", "Access denied", "NO_TOKEN");
            return;
        }

        try {
            Optional<ActiveToken> activeTokenOpt = tokenRepo.findByToken(token);

            if (activeTokenOpt.isEmpty()) {
                logger.warn("❌ [JWT] Token no encontrado en BD.");
                throw new JwtException("TOKEN_INACTIVE");
            }

            ActiveToken activeToken = activeTokenOpt.get();

    

            if (activeToken.getExpiresAt().before(new Date())) {
                logger.warn("⏰ [JWT] Token expirado. Eliminando...");
                tokenRepo.delete(activeToken);
                throw new JwtException("TOKEN_EXPIRED");
            }

            // Validación del JWT y carga del contexto
            Claims claims = Jwts.parser()
                    .setSigningKey(TokenJwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<String> roles = (List<String>) claims.get("authorities");
            logger.debug("👮 [JWT] Roles del usuario: {}", roles);

            var auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null,
                    roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

            SecurityContextHolder.getContext().setAuthentication(auth);
            logger.debug("✅ [JWT] Contexto de seguridad seteado correctamente.");
            chain.doFilter(req, res);

        } catch (JwtException e) {
            String code = e.getMessage();
            String message;
            String error;

            switch (code) {
                case "TOKEN_INACTIVE" -> {
                    message = "El token no está activo";
                    error = "Token inválido o deshabilitado";
                }
                case "TOKEN_EXPIRED" -> {
                    message = "El token expiró";
                    error = "Sesión caducada";
                }

                default -> {
                    message = "El token es inválido";
                    error = "No se pudo validar el token";
                    code = "INVALID_TOKEN";
                }
            }

            logger.error("❌ [JWT ERROR] {} - {}", code, error);
            sendError(res, HttpServletResponse.SC_UNAUTHORIZED, message, error, code);
        }
    }


    /**
     * Envía una respuesta de error JSON al cliente.
     * 
     * @param res HttpServletResponse para escribir la respuesta
     * @param status Código de estado HTTP
     * @param message Mensaje de error para el usuario
     * @param error Descripción del error
     * @param code Código de error específico
     * @throws IOException si hay error escribiendo la respuesta
     */
    private void sendError(HttpServletResponse res, int status, String message, String error, String code)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        new ObjectMapper().writeValue(res.getOutputStream(),
                Map.of("message", message, "error", error, "code", code));
    }

    /**
     * Extrae el token JWT de las cookies de la petición.
     * 
     * @param request HttpServletRequest con las cookies
     * @return Token JWT si existe en la cookie "access_token", null si no se encuentra
     */
    private String obtenerTokenDeCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
