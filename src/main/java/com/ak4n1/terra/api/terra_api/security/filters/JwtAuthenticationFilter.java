package com.ak4n1.terra.api.terra_api.security.filters;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserDisabledException;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Filtro de autenticación JWT que maneja el proceso de login.
 * 
 * <p>Este filtro intercepta las peticiones de login, valida las credenciales,
 * genera tokens JWT (access y refresh) y los guarda como cookies httpOnly.
 * También registra la actividad de login en el sistema.
 * 
 * @see UsernamePasswordAuthenticationFilter
 * @see com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig
 * @see com.ak4n1.terra.api.terra_api.security.filters.JwtValidationFilter
 * @author ak4n1
 * @since 1.0
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    private final AuthenticationManager authManager;
    private final ActiveTokenRepository tokenRepo;
    private final AccountMasterRepository userRepo;
    private final RecentActivityRepository activityRepo;

    /**
     * Constructor que inicializa el filtro con las dependencias necesarias.
     * 
     * <p>Configura el filtro para procesar peticiones a "/api/auth/login".
     * 
     * @param authManager Gestor de autenticación
     * @param tokenRepo Repositorio de tokens activos
     * @param userRepo Repositorio de usuarios
     * @param activityRepo Repositorio de actividad reciente
     */
    public JwtAuthenticationFilter(AuthenticationManager authManager,
                                   ActiveTokenRepository tokenRepo,
                                   AccountMasterRepository userRepo,
                                   RecentActivityRepository activityRepo) {
        this.authManager = authManager;
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.activityRepo = activityRepo;
        setFilterProcessesUrl("/api/auth/login");
    }

    /**
     * Intenta autenticar al usuario con las credenciales recibidas.
     * 
     * <p>Lee las credenciales del body de la petición (email y password) y las
     * envía al AuthenticationManager para validación.
     * 
     * @param req HttpServletRequest con las credenciales
     * @param res HttpServletResponse (no utilizado)
     * @return Authentication si la autenticación fue exitosa
     * @throws AuthenticationException si las credenciales son inválidas, email no verificado o cuenta deshabilitada
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {
            AccountMaster login = new ObjectMapper().readValue(req.getInputStream(), AccountMaster.class);

            return authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getEmail(),
                            login.getPassword()
                    )
            );

        } catch (InternalAuthenticationServiceException e) {
            Throwable cause = e.getCause();
            if (cause instanceof EmailNotVerifiedException) {
                logger.warn("❌ [LOGIN ERROR] Email no verificado");
                throw new BadCredentialsException("EMAIL_NOT_VERIFIED", cause);
            } else if (cause instanceof UserDisabledException) {
                logger.warn("❌ [LOGIN ERROR] Usuario deshabilitado");
                throw new DisabledException("USER_DISABLED", cause);
            }
            throw e;

        } catch (IOException e) {
            logger.error("❌ [LOGIN ERROR] Error leyendo credenciales: {}", e.getMessage(), e);
            throw new AuthenticationServiceException("Error leyendo credenciales", e);

        } catch (AuthenticationException e) {
            throw e;
        }
    }


    /**
     * Maneja la autenticación exitosa generando tokens JWT y guardándolos como cookies.
     * 
     * <p>Genera un access token y un refresh token, los guarda como cookies httpOnly,
     * almacena el token en la base de datos y registra la actividad de login.
     * 
     * @param req HttpServletRequest de la petición
     * @param res HttpServletResponse para enviar las cookies
     * @param chain FilterChain para continuar el procesamiento
     * @param auth Autenticación exitosa con los datos del usuario
     * @throws IOException si hay error escribiendo la respuesta
     * @throws ServletException si hay error en el procesamiento
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String email = auth.getName();
        List<String> roles = auth.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        logger.info("✅ [LOGIN SUCCESS] Usuario autenticado: {}", email);

        // Guardar token activo con manejo de errores
        userRepo.findByEmail(email).ifPresent(user -> {
            try {
                // Establecer el tipo de dispositivo
                String deviceType = "WEB";
                
                // Eliminar tokens del mismo tipo para este usuario
                List<ActiveToken> existingTokens = tokenRepo.findByAccountMaster_Email(email);
                existingTokens.stream()
                    .filter(token -> deviceType.equals(token.getDeviceType()))
                    .forEach(token -> tokenRepo.delete(token));
                
                // Generar token inicial
                String token = Jwts.builder()
                        .setSubject(email)
                        .claim("authorities", roles)
                        .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                        .signWith(TokenJwtConfig.SECRET_KEY)
                        .compact();
                
                // Verificar si el token ya existe
                Optional<ActiveToken> existingToken = tokenRepo.findByToken(token);
                if (existingToken.isPresent()) {
                    logger.warn("⚠️ [TOKEN DUPLICATE] Token ya existe, regenerando...");
                    // Regenerar token único
                    token = Jwts.builder()
                            .setSubject(email)
                            .claim("authorities", roles)
                            .claim("timestamp", System.currentTimeMillis()) // Agregar timestamp para unicidad
                            .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                            .signWith(TokenJwtConfig.SECRET_KEY)
                            .compact();
                }

                ActiveToken at = new ActiveToken();
                at.setAccountMaster(user);
                at.setToken(token);
                at.setCreatedAt(new Date());
                at.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION));
                
                // Establecer el tipo de dispositivo
                at.setDeviceType("WEB");

                tokenRepo.save(at);
                RecentActivity activity = new RecentActivity();
                activity.setAccountMaster(user);
                activity.setTimestamp(new Date());
                activity.setIpAddress(req.getRemoteAddr());
                
                activity.setAction("Web Login");
                
                activityRepo.save(activity);
                
                // Configurar cookie con el token generado
                Cookie cookie = new Cookie("access_token", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));
                res.addCookie(cookie);

                // Generar y configurar refresh token
                String refreshToken = Jwts.builder()
                        .setSubject(email)
                        .claim("type", "refresh")
                        .claim("timestamp", System.currentTimeMillis())
                        .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION))
                        .signWith(TokenJwtConfig.SECRET_KEY)
                        .compact();

                Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge((int) (TokenJwtConfig.REFRESH_TOKEN_EXPIRATION / 1000));
                res.addCookie(refreshCookie);


                
            } catch (DataIntegrityViolationException e) {
                logger.warn("❌ [TOKEN ERROR] Error de integridad, limpiando y reintentando...");
                // Si falla por constraint, limpiar y reintentar
                try {
                    tokenRepo.deleteOldTokensByUserId(user.getId());
                    Thread.sleep(100); // Pequeña pausa
                    
                    String retryToken = Jwts.builder()
                            .setSubject(email)
                            .claim("authorities", roles)
                            .claim("timestamp", System.currentTimeMillis())
                            .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                            .signWith(TokenJwtConfig.SECRET_KEY)
                            .compact();
                    
                    ActiveToken at = new ActiveToken();
                    at.setAccountMaster(user);
                    at.setToken(retryToken);
                    at.setCreatedAt(new Date());
                    at.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION));
                    
                    tokenRepo.save(at);
                    logger.info("✅ [TOKEN RECUPERADO] Token guardado después de limpieza");
                    
                    // Configurar cookie con el token de retry
                    Cookie cookie = new Cookie("access_token", retryToken);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));
                    res.addCookie(cookie);
                    
                    // Generar refresh token para retry también
                    String refreshToken = Jwts.builder()
                            .setSubject(email)
                            .claim("type", "refresh")
                            .claim("timestamp", System.currentTimeMillis())
                            .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION))
                            .signWith(TokenJwtConfig.SECRET_KEY)
                            .compact();

                    Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setPath("/");
                    refreshCookie.setMaxAge((int) (TokenJwtConfig.REFRESH_TOKEN_EXPIRATION / 1000));
                    res.addCookie(refreshCookie);
                    
                } catch (Exception retryException) {
                    logger.error("❌ [TOKEN CRITICAL] Error crítico guardando token: {}", retryException.getMessage(), retryException);
                }
            } catch (Exception e) {
                logger.error("❌ [TOKEN ERROR] Error inesperado: {}", e.getMessage(), e);
            }
        });

        Map<String, String> body = Map.of(
                "message", "Authentication successful",
                "email", email
        );

        res.setContentType(TokenJwtConfig.CONTENT_TYPE);
        new ObjectMapper().writeValue(res.getOutputStream(), body);
    }

    /**
     * Maneja la autenticación fallida enviando el error apropiado al cliente.
     * 
     * <p>Distingue entre diferentes tipos de errores (credenciales inválidas,
     * email no verificado, cuenta deshabilitada) y envía mensajes específicos.
     * 
     * @param req HttpServletRequest de la petición
     * @param res HttpServletResponse para enviar el error
     * @param failed Excepción de autenticación con el motivo del fallo
     * @throws IOException si hay error escribiendo la respuesta
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req,
                                              HttpServletResponse res,
                                              AuthenticationException failed) throws IOException {
        String userMessage;

        switch (failed.getMessage()) {
            case "EMAIL_NOT_VERIFIED":
                userMessage = "Email is not verified.";
                logger.warn("❌ [LOGIN FAILED] Email not verified.");
                break;
            case "USER_DISABLED":
                userMessage = "User account is disabled.";
                logger.warn("❌ [LOGIN FAILED] User disabled.");
                break;
            default:
                userMessage = "Invalid credentials.";
                logger.warn("❌ [LOGIN FAILED] Invalid credentials.");
                break;
        }

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, String> body = Map.of(
                "message", userMessage,
                "error", "LOGIN_FAILED"
        );
        res.setContentType(TokenJwtConfig.CONTENT_TYPE);
        new ObjectMapper().writeValue(res.getOutputStream(), body);
    }
}
