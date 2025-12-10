package com.ak4n1.terra.api.terra_api.security.config;

import com.ak4n1.terra.api.terra_api.auth.repositories.*;
import com.ak4n1.terra.api.terra_api.auth.repositories.RefreshTokenRepository;
import com.ak4n1.terra.api.terra_api.security.filters.*;
import com.ak4n1.terra.api.terra_api.security.filters.JwtAuthenticationFilter;
import com.ak4n1.terra.api.terra_api.security.filters.RateLimitFilter;
import com.ak4n1.terra.api.terra_api.security.filters.SecurityHeadersFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Configuración principal de seguridad de Spring Security.
 * 
 * <p>Esta clase configura la seguridad de la aplicación, incluyendo:
 * <ul>
 *   <li>Cadena de filtros de seguridad con autenticación JWT</li>
 *   <li>Configuración CORS para comunicación con el frontend</li>
 *   <li>Sesiones stateless con JWT</li>
 *   <li>Rutas públicas y protegidas</li>
 * </ul>
 * 
 * @see SecurityFilterChain
 * @see JwtAuthenticationFilter
 * @see JwtValidationFilter
 * @see TokenJwtConfig
 * @author ak4n1
 * @since 1.0
 */
@Configuration
public class SecurityConfig {

    private final ActiveTokenRepository activeTokenRepo;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountMasterRepository userRepo;
    private final RecentActivityRepository activityRepository;
    private final RateLimitFilter rateLimitFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    /**
     * Constructor que recibe las dependencias necesarias para la configuración de seguridad.
     * 
     * @param t Repositorio de tokens activos (access tokens)
     * @param rt Repositorio de refresh tokens
     * @param u Repositorio de usuarios (AccountMaster)
     * @param r Repositorio de actividad reciente
     */
    public SecurityConfig(ActiveTokenRepository t, RefreshTokenRepository rt, AccountMasterRepository u, 
                         RecentActivityRepository r, RateLimitFilter rateLimitFilter, 
                         SecurityHeadersFilter securityHeadersFilter) {
        this.activeTokenRepo = t;
        this.refreshTokenRepository = rt;
        this.userRepo = u;
        this.activityRepository = r;
        this.rateLimitFilter = rateLimitFilter;
        this.securityHeadersFilter = securityHeadersFilter;
    }

    /**
     * Bean para el codificador de contraseñas usando BCrypt.
     * 
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para el gestor de autenticación de Spring Security.
     * 
     * @param cfg Configuración de autenticación
     * @return AuthenticationManager configurado
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Configura la cadena de filtros de seguridad principal de la aplicación.
     * 
     * <p>Define las rutas públicas y protegidas, configura CORS, deshabilita CSRF
     * (porque usamos JWT stateless), y añade los filtros JWT para autenticación y validación.
     * 
     * @param http HttpSecurity para configurar
     * @param authManager Gestor de autenticación
     * @return SecurityFilterChain configurado
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        var jwtAuthFilter = new JwtAuthenticationFilter(authManager, activeTokenRepo, refreshTokenRepository, userRepo, activityRepository);
        var jwtValFilter = new JwtValidationFilter(authManager, activeTokenRepo, userRepo);

        http
                // CORS habilitado nuevamente
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitamos CSRF porque JWT stateless con cookie no usa el token de Spring
                .csrf(csrf -> csrf.disable())
                
                // Security Headers: protecciones adicionales HTTP
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny()) // Previene clickjacking (X-Frame-Options: DENY)
                        .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                        .httpStrictTransportSecurity(hsts -> hsts.disable()) // Solo en producción con HTTPS
                )

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/logout",
                                "/api/auth/resend-reset-email",
                                "/api/auth/reset-password",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/google/login",
                                "/api/auth/refresh",
                                "/api/test/**",
                                "/api/kick/channels/**",
                                "/api/game/ranking/top-pvp",
                                "/api/game/ranking/top-pk",
                                "/api/game/ranking/top-clans",
                                "/api/game/patch-notes",
                                "/api/stats",
                                "/api/payments/webhook",
                                "/api/payments/webhook/**",
                                "/api/webhooks/**"


                        ).permitAll()
                        
                        // ========================================
                        // ENDPOINTS USER (Requieren autenticación)
                        // ========================================
                        
                        // WebSocket endpoint - Se permite el handshake inicial (upgrade HTTP)
                        // pero la autenticación se valida en WebSocketHandshakeInterceptor
                        // Si no hay token válido, el interceptor rechaza el handshake
                        .requestMatchers("/api/notifications/ws").permitAll()
                        
                        // USER - GET
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/me",
                                "/api/auth/getme",
                                "/api/auth/recent-activity",
                                "/api/game/auth/accounts",
                                "/api/game/characters/by-email",
                                "/api/game/characters/by-email/paginated",
                                "/api/game/characters/by-email/stats",
                                "/api/game/characters/by-email/complete",
                                "/api/game/offline-market",
                                "/api/game/offline-market/paginated",
                                "/api/payments/packages",
                                "/api/payments/methods",
                                "/api/payments/packages/popular",
                                "/api/payments/packages/*",
                                "/api/payments/history",
                                "/api/payments/history/paginated",
                                "/api/payments/transaction/*/status",
                                "/api/payments/transaction/*/resume",
                                "/api/streamer-applications/my-applications",
                                "/api/withdrawal-permissions",
                                "/api/news/latest",
                                "/api/news",
                                "/api/news/**",
                                "/api/notifications/unread",
                                "/api/notifications/unread/count",
                                "/api/notifications/export"
                        ).hasRole("USER")
                        
                        // USER - POST
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/change-password",
                                "/api/auth/deactivate-code",
                                "/api/auth/deactivate-verify",
                                "/api/game/auth/registerGameAccount",
                                "/api/game/auth/reset-code",
                                "/api/game/auth/create-code",
                                "/api/game/auth/changePassword",
                                "/api/game/clan/by-id",
                                "/api/game/storage/inventory",
                                "/api/game/skills/character",
                                "/api/game/subclasses/character",
                                "/api/payments/create-preference",
                                "/api/payments/paypal/capture/**",
                                "/api/streamer-applications",
                                "/api/withdrawal/generate-code",
                                "/api/withdrawal-permissions/grant",
                                "/api/withdrawal-permissions/revoke",
                                "/api/notifications/*/read",
                                "/api/notifications/read-all"
                        ).hasRole("USER")
                        
                        // USER - DELETE
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/notifications/all"
                        ).hasRole("USER")
                        
                        // ========================================
                        // ENDPOINTS ADMIN (Requieren rol ADMIN)
                        // ========================================
                        
                        // Actuator endpoints - Solo ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // ADMIN - GET
                        .requestMatchers(HttpMethod.GET,
                                "/api/streamer-applications/admin/approved",
                                "/api/streamer-applications/admin/pending",
                                "/api/streamer-applications/admin/rejected"
                        ).hasRole("ADMIN")
                        
                        // ADMIN - POST
                        .requestMatchers(HttpMethod.POST,
                                "/api/news",
                                "/api/news/*/publish",
                                "/api/news/*/unpublish",
                                "/api/payments/admin/**",
                                "/api/payments/transaction/*/refund",
                                "/api/game/catalog/items/admin/**",
                                "/api/notifications/admin/create",
                                "/api/notifications/admin/create-broadcast"
                        ).hasRole("ADMIN")
                        
                        // ADMIN - PUT
                        .requestMatchers(HttpMethod.PUT,
                                "/api/news/**"
                        ).hasRole("ADMIN")
                        
                        // ADMIN - DELETE
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/news/**"
                        ).hasRole("ADMIN")
                        

                )

                // Filtros de seguridad en orden
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class) // Security Headers 
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Rate Limiting
                .addFilterAt(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // JWT Authentication 
                .addFilterAfter(jwtValFilter, JwtAuthenticationFilter.class); // JWT Validation 

        return http.build();
    }

    /**
     * Configura el origen de configuración CORS para permitir peticiones desde el frontend.
     * 
     * <p>Permite orígenes específicos (localhost, l2terra.online, etc.) y métodos HTTP.
     * Habilita el envío de credenciales (cookies) necesarias para los tokens JWT.
     * 
     * @return CorsConfigurationSource configurado con los orígenes permitidos
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "file://" ,//Electron
                "https://l2terra.online",
                "http://localhost:4200"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // Importante para mandar cookies con credenciales
        config.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


}
