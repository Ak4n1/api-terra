package com.ak4n1.terra.api.terra_api.auth.controllers;

import com.ak4n1.terra.api.terra_api.auth.dto.RecentActivityDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterResponseDTO;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import com.ak4n1.terra.api.terra_api.auth.entities.RefreshToken;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.RefreshTokenReusedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserDisabledException;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RefreshTokenRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import com.ak4n1.terra.api.terra_api.auth.services.AuthService;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

/**
 * Controlador REST para operaciones de autenticación y gestión de usuarios.
 * 
 * <p>Gestiona el registro, login, logout, verificación de email, recuperación de contraseña
 * y gestión de tokens JWT (incluyendo refresh tokens). Es el controlador RECOMENDADO para
 * todas las operaciones de autenticación de usuarios.
 * 
 * @see AuthService
 * @see TokenJwtConfig
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private RecentActivityRepository recentActivityRepository;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Valida los datos de entrada y llama al servicio para crear la cuenta.
     * Envía un email de verificación al usuario registrado.
     * 
     * @param registerRequest DTO con email y contraseña del nuevo usuario
     * @param result Resultado de la validación de Bean Validation
     * @return ResponseEntity con el resultado del registro (CREATED si es exitoso, BAD_REQUEST si hay errores)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO registerRequest, BindingResult result) {
        // Validación de campos
        if (result.hasErrors()) {
            return validation(result); // Si hay errores, devuelve los errores
        }

        System.out.println("entro al registro controller");
        // Llamar al servicio para registrar la cuenta y devolver la respuesta
        return authService.save(registerRequest); // Crear la cuenta y devolver la respuesta
    }

    /**
     * Construye una respuesta de error con los mensajes de validación.
     * 
     * @param result Resultado de la validación con los errores
     * @return ResponseEntity con status BAD_REQUEST y los mensajes de error
     */
    private ResponseEntity<?> validation(BindingResult result) {
        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        StringBuilder errorMessage = new StringBuilder();
        result.getFieldErrors().forEach(err -> {
            errorMessage.append("El campo ").append(err.getField()).append(" ").append(err.getDefaultMessage())
                    .append(" ");
        });

        response.setMessage(errorMessage.toString()); // Establecer todos los errores en un solo mensaje
        return ResponseEntity.badRequest().body(response); // Devolver el DTO con los errores
    }

    /**
     * Cierra la sesión del usuario eliminando los tokens.
     * 
     * <p>Elimina el token activo de la base de datos y expira las cookies
     * tanto del access token como del refresh token en el cliente.
     * 
     * @param token Access token desde la cookie
     * @param refreshToken Refresh token desde la cookie
     * @param response HttpServletResponse para eliminar las cookies
     * @return ResponseEntity con mensaje de logout exitoso
     */
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "access_token", required = false) String token,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        // Eliminar access token de BD
        if (token != null) {
            activeTokenRepository.deleteByToken(token);
            logger.debug("🗑️ [LOGOUT] Access token eliminado de BD");
        }

        // Revocar refresh token de BD
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenEntity -> {
                refreshTokenEntity.setRevoked(true);
                refreshTokenRepository.save(refreshTokenEntity);
                logger.debug("🗑️ [LOGOUT] Refresh token revocado en BD");
            });
        }

        // Eliminar cookie access_token en el cliente
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(TokenJwtConfig.USE_SECURE_COOKIES);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0); // expira ya
        response.addCookie(accessCookie);

        // Eliminar cookie refresh_token en el cliente
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(TokenJwtConfig.USE_SECURE_COOKIES);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // expira ya
        response.addCookie(refreshCookie);

        logger.info("✅ [LOGOUT] Logout exitoso");
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    /**
     * Obtiene la información del usuario autenticado actual.
     * 
     * @param authentication Autenticación de Spring Security con los datos del usuario
     * @return ResponseEntity con los datos del usuario actual o UNAUTHORIZED si no está autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String email = (String) authentication.getPrincipal();
        Map<String, Object> userData = authService.getCurrentUser(email);
        
        return ResponseEntity.ok(userData);
    }

    /**
     * Reenvía el email de recuperación de contraseña.
     * 
     * @param email Email del usuario que solicita el reseteo
     * @return ResponseEntity con el resultado (OK si se envió, BAD_REQUEST si hay error)
     */
    @PostMapping("/resend-reset-email")
    public ResponseEntity<?> resendResetEmail(@RequestParam String email) {
        Map<String, Object> response = authService.sendPasswordResetEmail(email);

        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Restablece la contraseña del usuario usando un token de reseteo.
     * 
     * @param tokenUser Token de reseteo de contraseña recibido por email
     * @param requestBody Cuerpo de la petición con la nueva contraseña
     * @return ResponseEntity con el resultado (OK si se cambió, BAD_REQUEST si el token es inválido o expiró)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam("token") String tokenUser,
            @RequestBody Map<String, Object> requestBody) {

        String password = (String) requestBody.get("password");

        Map<String, Object> response = authService.resetPassword(tokenUser, password);

        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Reenvía el email de verificación al usuario.
     * 
     * @param body Cuerpo de la petición con el email del usuario
     * @return ResponseEntity con el resultado (OK si se envió, FORBIDDEN si hay restricción de tiempo, BAD_REQUEST si hay error)
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, String> result = authService.resendVerificationEmail(email);
        System.out.println();
        switch (result.get("status")) {
            case "success":
                return ResponseEntity.ok(result);
            case "forbidden":
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            default:
                return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Verifica el email del usuario usando el token de verificación.
     * 
     * @param token Token de verificación recibido por email
     * @return ResponseEntity con el resultado de la verificación
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    /**
     * Obtiene el historial de actividad reciente del usuario autenticado.
     * 
     * @param authentication Autenticación de Spring Security con los datos del usuario
     * @return ResponseEntity con la lista de actividades recientes o UNAUTHORIZED si no está autenticado
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String email = (String) authentication.getPrincipal();
        List<RecentActivity> activities = recentActivityRepository
                .findByAccountMaster_EmailOrderByTimestampDesc(email);

        List<RecentActivityDTO> activityDTOs = activities.stream()
                .map(act -> new RecentActivityDTO(
                        act.getAction(),
                        new Timestamp(act.getTimestamp().getTime()),
                        act.getIpAddress()))
                .toList();

        return ResponseEntity.ok(activityDTOs);
    }

    /**
     * Renueva el access token usando el refresh token.
     * 
     * <p>Valida el refresh token, genera un nuevo access token y lo guarda en la base de datos
     * y como cookie. Elimina los tokens antiguos del usuario.
     * 
     * @param refreshToken Refresh token desde la cookie
     * @param request HttpServletRequest para obtener IP del cliente
     * @param response HttpServletResponse para configurar la nueva cookie de access token
     * @return ResponseEntity con el resultado (OK si se renovó, UNAUTHORIZED si el refresh token es inválido o expiró)
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token not provided", "error", "REFRESH_TOKEN_MISSING"));
        }

        try {
            // Validar firma JWT del refresh token
            Claims claims = Jwts.parser()
                    .setSigningKey(TokenJwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String email = claims.getSubject();
            
            // VALIDACIÓN EN BD: Buscar refresh token en base de datos
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshToken);
            
            if (refreshTokenOpt.isEmpty()) {
                logger.warn("❌ [REFRESH] Refresh token no encontrado en BD: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token invalid or revoked", "error", "REFRESH_TOKEN_INVALID"));
            }

            RefreshToken refreshTokenEntity = refreshTokenOpt.get();
            
            // Validar que no esté revocado (ANTES de hacer cualquier cambio)
            if (refreshTokenEntity.isRevoked()) {
                logger.warn("❌ [REFRESH] Refresh token revocado (reuso detectado): {}", email);
                throw new RefreshTokenReusedException("Refresh token ya fue usado y no puede reutilizarse");
            }
            
            // Validar que no esté expirado (validación en BD además de JWT)
            if (refreshTokenEntity.getExpiresAt().before(new Date())) {
                logger.warn("⏰ [REFRESH] Refresh token expirado en BD: {}", email);
                refreshTokenRepository.delete(refreshTokenEntity);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token expired", "error", "REFRESH_TOKEN_EXPIRED"));
            }

            Optional<AccountMaster> userOpt = accountMasterRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found", "error", "USER_NOT_FOUND"));
            }

            AccountMaster user = userOpt.get();
            
            // SECURITY FIX: Validar estado del usuario
            if (!user.isEnabled()) {
                logger.warn("❌ [REFRESH] Usuario deshabilitado: {}", email);
                // Revocar todos los tokens del usuario
                refreshTokenRepository.revokeAllByUserId(user.getId());
                activeTokenRepository.deleteOldTokensByUserId(user.getId());
                throw new UserDisabledException("Cuenta deshabilitada");
            }

            if (!user.isEmailVerified()) {
                logger.warn("❌ [REFRESH] Email no verificado: {}", email);
                // Revocar todos los tokens del usuario
                refreshTokenRepository.revokeAllByUserId(user.getId());
                activeTokenRepository.deleteOldTokensByUserId(user.getId());
                throw new EmailNotVerifiedException("Email no verificado");
            }
            
            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            // ROTACIÓN: Invalidar refresh token usado (OPERACIÓN ATÓMICA para evitar race condition)
            refreshTokenEntity.setRevoked(true);
            RefreshToken savedToken = refreshTokenRepository.saveAndFlush(refreshTokenEntity); // flush inmediato para atomicidad
            
            // Double-check después del save (protección contra race condition)
            if (!savedToken.isRevoked()) {
                logger.error("❌ [REFRESH CRITICAL] Error atómico: token no se marcó como revocado");
                throw new RefreshTokenReusedException("Error en rotación de token");
            }
            
            logger.debug("🔄 [REFRESH] Refresh token anterior marcado como revocado (atómico)");

            // Generar nuevo access token
            String newAccessToken = Jwts.builder()
                    .setSubject(email)
                    .claim("authorities", roles)
                    .claim("timestamp", System.currentTimeMillis())
                    .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            // Eliminar access tokens viejos y guardar el nuevo
            activeTokenRepository.deleteOldTokensByUserId(user.getId());

            ActiveToken newActiveToken = new ActiveToken();
            newActiveToken.setAccountMaster(user);
            newActiveToken.setToken(newAccessToken);
            newActiveToken.setCreatedAt(new Date());
            newActiveToken.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION));
            newActiveToken.setDeviceType(refreshTokenEntity.getDeviceType()); // Mantener el mismo tipo de dispositivo
            activeTokenRepository.save(newActiveToken);

            // ROTACIÓN: Generar nuevo refresh token
            String newRefreshToken = Jwts.builder()
                    .setSubject(email)
                    .claim("type", "refresh")
                    .claim("timestamp", System.currentTimeMillis())
                    .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            // Guardar nuevo refresh token en BD
            RefreshToken newRefreshTokenEntity = new RefreshToken();
            newRefreshTokenEntity.setAccountMaster(user);
            newRefreshTokenEntity.setToken(newRefreshToken);
            newRefreshTokenEntity.setCreatedAt(new Date());
            newRefreshTokenEntity.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.REFRESH_TOKEN_EXPIRATION));
            newRefreshTokenEntity.setDeviceType(refreshTokenEntity.getDeviceType()); // Mantener el mismo tipo de dispositivo
            newRefreshTokenEntity.setRevoked(false);
            refreshTokenRepository.save(newRefreshTokenEntity);

            // Configurar cookie access token
            Cookie cookie = new Cookie("access_token", newAccessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(TokenJwtConfig.USE_SECURE_COOKIES);
            cookie.setPath("/");
            cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));
            response.addCookie(cookie);

            // Configurar cookie refresh token (nuevo)
            Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(TokenJwtConfig.USE_SECURE_COOKIES);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) (TokenJwtConfig.REFRESH_TOKEN_EXPIRATION / 1000));
            response.addCookie(refreshCookie);

            // SECURITY FIX: Registrar actividad de refresh
            RecentActivity activity = new RecentActivity();
            activity.setAccountMaster(user);
            activity.setTimestamp(new Date());
            activity.setIpAddress(request.getRemoteAddr());
            activity.setAction("Token Refresh");
            recentActivityRepository.save(activity);

            logger.info("🔄 [REFRESH SUCCESS] Tokens renovados para: {}", email);

            return ResponseEntity.ok(Map.of("message", "Token renewed successfully"));

        } catch (RefreshTokenReusedException e) {
            logger.warn("❌ [REFRESH] Refresh token reusado: {}", e.getMessage());
            throw e; // Lanzar para que GlobalExceptionHandler lo maneje
        } catch (UserDisabledException e) {
            logger.warn("❌ [REFRESH] Usuario deshabilitado");
            throw e; // Lanzar para que GlobalExceptionHandler lo maneje
        } catch (EmailNotVerifiedException e) {
            logger.warn("❌ [REFRESH] Email no verificado");
            throw e; // Lanzar para que GlobalExceptionHandler lo maneje
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token expired", "error", "REFRESH_TOKEN_EXPIRED"));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token invalid", "error", "REFRESH_TOKEN_INVALID"));
        } catch (Exception e) {
            logger.error("❌ [REFRESH ERROR] Error renovando token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal error renewing token", "error", "INTERNAL_ERROR"));
        }
    }

}