package com.ak4n1.terra.api.terra_api.security.config;

import com.ak4n1.terra.api.terra_api.auth.repositories.*;
import com.ak4n1.terra.api.terra_api.security.filters.*;
import com.ak4n1.terra.api.terra_api.security.filters.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
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

@Configuration
public class SecurityConfig {

    private final ActiveTokenRepository activeTokenRepo;
    private final AccountMasterRepository userRepo;
    private final RecentActivityRepository activityRepository;

    public SecurityConfig(ActiveTokenRepository t, AccountMasterRepository u, RecentActivityRepository r) {
        this.activeTokenRepo = t;
        this.userRepo = u;
        this.activityRepository = r;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        var jwtAuthFilter = new JwtAuthenticationFilter(authManager, activeTokenRepo, userRepo, activityRepository);
        var jwtValFilter = new JwtValidationFilter(authManager, activeTokenRepo);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitamos CSRF porque JWT stateless con cookie no usa el token de Spring
                .csrf(csrf -> csrf.disable())

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
                                "/api/kick/channels/**",
                                "/api/game/ranking/top-pvp",
                                "/api/game/ranking/top-pk",
                                "/api/game/ranking/top-clans",
                                "/api/game/patch-notes",
                                "/api/stats",
                                "/api/payments/webhook",
                                "/api/payments/webhook/**"



                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/me",
                                "/api/game/auth/registerGameAccount",
                                "/api/game/auth/accounts",
                                "/api/game/auth/reset-code",
                                "/api/game/auth/create-code",
                                "/api/game/auth/changePassword",
                                "/api/auth/recent-activity",
                                "/api/game/characters/by-email",
                                "/api/game/characters/by-email/paginated",
                                "/api/game/characters/by-email/stats",
                                "/api/game/characters/by-email/complete",
                                "/api/game/clan/by-id",
                                "/api/game/offline-market",
                                "/api/game/storage/inventory",
                                "/api/payments/packages",
                                "/api/payments/methods",
                                "/api/payments/create-preference"



                        ).authenticated()

                )

                // Filtros JWT
                .addFilterAt(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // JWT Authentication (login)
                .addFilterAfter(jwtValFilter, JwtAuthenticationFilter.class); // JWT Validation (después del login)

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "file://" ,//Electron
                "http://localhost:4200",
                "https://l2terra.online",
                "http://192.168.100.84:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // Importante para mandar cookies con credenciales
        config.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


}
