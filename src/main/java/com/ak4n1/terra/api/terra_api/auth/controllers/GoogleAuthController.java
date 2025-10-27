package com.ak4n1.terra.api.terra_api.auth.controllers;

import com.ak4n1.terra.api.terra_api.auth.entities.*;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
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

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    private final AccountMasterRepository accountMasterRepository;
    private final ActiveTokenRepository activeTokenRepository;
    private final RecentActivityRepository recentActivityRepository;
    private final FirebaseAuth firebaseAuth;
  
    @Autowired
    private RoleRepository roleRepository;

    public GoogleAuthController(AccountMasterRepository accountMasterRepository,
            ActiveTokenRepository activeTokenRepository,
            RecentActivityRepository recentActivityRepository,
            FirebaseAuth firebaseAuth) {
        this.accountMasterRepository = accountMasterRepository;
        this.activeTokenRepository = activeTokenRepository;
        this.recentActivityRepository = recentActivityRepository;
        this.firebaseAuth = firebaseAuth;

    }

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

            activeTokenRepository.save(activeToken);

            // Registrar actividad
            RecentActivity activity = new RecentActivity();
            activity.setAccountMaster(user);
            activity.setTimestamp(new Date());
            activity.setIpAddress(httpRequest.getRemoteAddr());
            activity.setAction("Google Login");
            recentActivityRepository.save(activity);

            // Configurar cookie
            Cookie cookie = new Cookie("access_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));

            httpResponse.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                    "message", "Autenticación exitosa con Google",
                    "email", email,
                    "name", name));

        } catch (FirebaseAuthException e) {
            logger.error("❌ [GOOGLE AUTH] Error verificando token: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body(Map.of("error", "Token de Google inválido"));
        } catch (Exception e) {
            logger.error("❌ [GOOGLE AUTH] Error interno: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

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
