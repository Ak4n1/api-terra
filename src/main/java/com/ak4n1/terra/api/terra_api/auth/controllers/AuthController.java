package com.ak4n1.terra.api.terra_api.auth.controllers;

import com.ak4n1.terra.api.terra_api.auth.dto.RecentActivityDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterResponseDTO;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.ActiveTokenRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
import com.ak4n1.terra.api.terra_api.security.config.TokenJwtConfig;
import com.ak4n1.terra.api.terra_api.auth.services.AuthService;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private RecentActivityRepository recentActivityRepository;

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

    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "access_token", required = false) String token,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (token != null) {
            activeTokenRepository.deleteByToken(token); // borrás el token activo en BD
        }

        // Borrás la cookie access_token en el cliente
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0); // expira ya
        response.addCookie(accessCookie);

        // Borrás la cookie refresh_token en el cliente
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // expira ya
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String email = (String) authentication.getPrincipal();
        Map<String, Object> userData = authService.getCurrentUser(email);
        
        return ResponseEntity.ok(userData);
    }

    @PostMapping("/resend-reset-email")
    public ResponseEntity<?> resendResetEmail(@RequestParam String email) {
        Map<String, Object> response = authService.sendPasswordResetEmail(email);

        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

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

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

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
     * Endpoint para refresh de tokens
     * Permite renovar el access token usando el refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token no proporcionado", "error", "REFRESH_TOKEN_MISSING"));
        }

        try {
            // Validar refresh token
            Claims claims = Jwts.parser()
                    .setSigningKey(TokenJwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String email = claims.getSubject();
            Optional<AccountMaster> userOpt = accountMasterRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Usuario no encontrado", "error", "USER_NOT_FOUND"));
            }

            AccountMaster user = userOpt.get();
            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            // Generar nuevo access token
            String newAccessToken = Jwts.builder()
                    .setSubject(email)
                    .claim("authorities", roles)
                    .claim("timestamp", System.currentTimeMillis())
                    .setExpiration(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            // Eliminar tokens viejos y guardar el nuevo
            activeTokenRepository.deleteOldTokensByUserId(user.getId());

            ActiveToken newActiveToken = new ActiveToken();
            newActiveToken.setAccountMaster(user);
            newActiveToken.setToken(newAccessToken);
            newActiveToken.setCreatedAt(new Date());
            newActiveToken.setExpiresAt(new Date(System.currentTimeMillis() + TokenJwtConfig.ACCESS_TOKEN_EXPIRATION));

            activeTokenRepository.save(newActiveToken);

            // Configurar nueva cookie
            Cookie cookie = new Cookie("access_token", newAccessToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (TokenJwtConfig.ACCESS_TOKEN_EXPIRATION / 1000));
            response.addCookie(cookie);

            System.out.println("🔄 [REFRESH SUCCESS] Token renovado para: " + email);

            return ResponseEntity.ok(Map.of(
                    "message", "Token renovado exitosamente",
                    "email", email));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token expirado", "error", "REFRESH_TOKEN_EXPIRED"));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token inválido", "error", "REFRESH_TOKEN_INVALID"));
        } catch (Exception e) {
            System.out.println("❌ [REFRESH ERROR] Error renovando token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno renovando token", "error", "INTERNAL_ERROR"));
        }
    }

}