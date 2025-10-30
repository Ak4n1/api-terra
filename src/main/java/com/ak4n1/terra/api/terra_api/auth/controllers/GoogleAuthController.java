package com.ak4n1.terra.api.terra_api.auth.controllers;

import com.ak4n1.terra.api.terra_api.auth.entities.*;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RefreshTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RoleRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.ak4n1.terra.api.terra_api.auth.entities.RefreshToken;

/**
 * Controlador REST para autenticación con Google OAuth.
 * 
 * <p>Gestiona el login de usuarios mediante Google Firebase Authentication.
 * Verifica el token ID de Google, crea o encuentra el usuario en el sistema
 * y genera tokens JWT para la sesión.
 * 
 * @see FirebaseAuth
 * @see com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    private final AccountMasterRepository accountMasterRepository;
    private final ActiveTokenRepository activeTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RecentActivityRepository recentActivityRepository;
    private final FirebaseAuth firebaseAuth;
  
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Constructor que recibe las dependencias necesarias para la autenticación con Google.
     * 
     * @param accountMasterRepository Repositorio de usuarios
     * @param activeTokenRepository Repositorio de tokens activos (access tokens)
     * @param refreshTokenRepository Repositorio de refresh tokens
     * @param recentActivityRepository Repositorio de actividad reciente
     * @param firebaseAuth Cliente de Firebase Authentication
     */
    public GoogleAuthController(AccountMasterRepository accountMasterRepository,
            ActiveTokenRepository activeTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            RecentActivityRepository recentActivityRepository,
            FirebaseAuth firebaseAuth) {
        this.accountMasterRepository = accountMasterRepository;
        this.activeTokenRepository = activeTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.recentActivityRepository = recentActivityRepository;
        this.firebaseAuth = firebaseAuth;

    }

    /**
     * Autentica un usuario usando el token ID de Google.
     * 
     * <p>Verifica el token con Firebase, crea o actualiza el usuario en el sistema
     * y genera un JWT para la sesión. Guarda el token y registra la actividad.
     * 
     * @param request Cuerpo de la petición con el idToken de Google
     * @param httpRequest HttpServletRequest para obtener la IP del cliente
     * @param httpResponse HttpServletResponse para configurar la cookie de acceso
     * @return ResponseEntity con el resultado de la autenticación
     * @throws IOException si hay error procesando la respuesta
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {

        String idToken = request.get("idToken");

        if (idToken == null || idToken.isEmpty()) {
            logger.warn("❌ [GOOGLE AUTH] No se recibió idToken");
            return ResponseEntity.badRequest().body(Map.of("error", "ID Token is required"));
        }

        try {

            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String uid = decodedToken.getUid();

            logger.info("✅ [GOOGLE AUTH] Token válido para: {}", email);

            AccountMaster user = findOrCreateUser(email, name, uid);

            logger.info("👤 [GOOGLE AUTH] Usuario encontrado o creado: {} (ID: {})", user.getEmail(), user.getId());

            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName())
                    .toList();

            String token = Jwts.builder()
                    .setSubject(email)
                    .claim("authorities", roles)
                    .claim("authMethod", "google")
                    .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            // Guardar token activo (elimina viejos, guarda nuevo)
            activeTokenRepository.deleteOldTokensByUserId(user.getId());

            ActiveToken activeToken = new ActiveToken();
            activeToken.setAccountMaster(user);
            activeToken.setToken(token);
            activeToken.setCreatedAt(new Date());
            activeToken.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION));
            activeToken.setDeviceType("WEB");
            activeTokenRepository.save(activeToken);

            // Generar y guardar refresh token
            String refreshToken = Jwts.builder()
                    .setSubject(email)
                    .claim("type", "refresh")
                    .claim("authMethod", "google")
                    .claim("timestamp", System.currentTimeMillis())
                    .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            // Eliminar refresh tokens previos del mismo usuario y dispositivo
            String deviceType = "WEB";
            List<RefreshToken> existingRefreshTokens = refreshTokenRepository.findByAccountMaster_Email(email);
            existingRefreshTokens.stream()
                .filter(rt -> deviceType.equals(rt.getDeviceType()))
                .forEach(rt -> refreshTokenRepository.delete(rt));

            // Guardar refresh token en BD
            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setAccountMaster(user);
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setCreatedAt(new Date());
            refreshTokenEntity.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION));
            refreshTokenEntity.setDeviceType(deviceType);
            refreshTokenEntity.setRevoked(false);
            refreshTokenRepository.save(refreshTokenEntity);

            // Registrar actividad
            RecentActivity activity = new RecentActivity();
            activity.setAccountMaster(user);
            activity.setTimestamp(new Date());
            activity.setIpAddress(httpRequest.getRemoteAddr());
            activity.setAction("Google Login");
            recentActivityRepository.save(activity);

            // Configurar cookie access token
            Cookie cookie = new Cookie("access_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig.USE_SECURE_COOKIES);
            cookie.setPath("/");
            cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));
            httpResponse.addCookie(cookie);

            // Configurar cookie refresh token
            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig.USE_SECURE_COOKIES);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) (TokenJwtConfig.REFRESH_TOKEN_EXPIRATION / 1000));
            httpResponse.addCookie(refreshCookie);

            return ResponseEntity.ok(Map.of(
                    "message", "Authentication successful with Google",
                    "email", email,
                    "name", name));

        } catch (FirebaseAuthException e) {
            logger.error("❌ [GOOGLE AUTH] Error verificando token: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        } catch (Exception e) {
            logger.error("❌ [GOOGLE AUTH] Error interno: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Busca un usuario existente por email o crea uno nuevo si no existe.
     * 
     * <p>Si el usuario existe pero no tiene googleUid, lo actualiza.
     * Si no existe, crea un nuevo usuario con email verificado y rol ROLE_USER.
     * 
     * @param email Email del usuario desde Google
     * @param name Nombre del usuario desde Google
     * @param googleUid UID único de Google del usuario
     * @return AccountMaster existente o recién creado
     */
    private AccountMaster findOrCreateUser(String email, String name, String googleUid) {
        Optional<AccountMaster> existingUser = accountMasterRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            AccountMaster user = existingUser.get();

            if (user.getGoogleUid() == null || user.getGoogleUid().isEmpty()) {
                user.setGoogleUid(googleUid);
                accountMasterRepository.save(user);
            }
            return user;
        } else {
            AccountMaster newUser = new AccountMaster();
            newUser.setEmail(email);
            newUser.setPassword("oauth_no_password"); // para evitar error null password
            newUser.setGoogleUid(googleUid);
            newUser.setEmailVerified(true);
            newUser.setEnabled(true);
            newUser.setCreatedAt(new Date());

            // **Asignar rol ROLE_USER obligatoriamente**
            Optional<Role> defaultRole = roleRepository.findByName("ROLE_USER");
            defaultRole.ifPresent(role -> newUser.setRoles(List.of(role)));

            return accountMasterRepository.save(newUser);
        }
    }

}
