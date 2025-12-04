package com.ak4n1.terra.api.terra_api.auth.services;

import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterResponseDTO;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.Role;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RoleRepository;

import com.ak4n1.terra.api.terra_api.utils.CodeGenerator;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountDeactivateCode;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountDeactivation;
import java.sql.Timestamp;
import com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailAlreadyExistsException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.TokenExpiredException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del servicio de autenticación.
 * 
 * <p>Proporciona la lógica de negocio para registro, verificación de email,
 * recuperación de contraseña y gestión de usuarios. Es la clase RECOMENDADA
 * para todas las operaciones de autenticación.
 * 
 * @see AuthService
 * @author ak4n1
 * @since 1.0
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService emailService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailContent emailContent;

    @Autowired
    private com.ak4n1.terra.api.terra_api.auth.repositories.AccountDeactivateCodeRepository accountDeactivateCodeRepository;

    @Autowired
    private com.ak4n1.terra.api.terra_api.auth.repositories.AccountDeactivationRepository accountDeactivationRepository;

    /**
     * {@inheritDoc}
     * 
     * <p>Verifica que el email no exista, crea el usuario con rol ROLE_USER,
     * codifica la contraseña y envía un email de verificación.
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.EmailAlreadyExistsException si el email ya existe
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(RegisterRequestDTO registerRequest) {
        RegisterResponseDTO response = new RegisterResponseDTO();

        // Check if email already exists
        if (accountMasterRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("❌ [REGISTER] Email already exists: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // Crear una nueva cuenta
        AccountMaster account = new AccountMaster();
        account.setEmail(registerRequest.getEmail());
        account.setPassword(registerRequest.getPassword());

        // Generar código de verificación
        String verificationCode = CodeGenerator.generateVerificationCode();
        account.setVerificationToken(verificationCode);

        // Establecer la expiración del token
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5); // 5 minutos
        account.setTokenExpiration(calendar.getTime());

        // Asignar roles
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        if (account.isAdmin()) {
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            optionalRoleAdmin.ifPresent(roles::add);
        }
        account.setRoles(roles);

        // Codificar la contraseña
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        // Enviar email de verificación (asíncrono, no bloquea)
        String subject = "Verify your email - L2 Terra";
        String body = emailContent.buildRegistrationVerificationEmailBody(verificationCode, account.getEmail());
        
        emailService.sendEmail(account.getEmail(), subject, body)
                .exceptionally(ex -> {
                    logger.error("❌ [REGISTER] Error enviando email de verificación a {}: {}", account.getEmail(), ex.getMessage());
                    return null;
                });


        // Registrar la fecha de creación
        account.setCreatedAt(new Date());
        accountMasterRepository.save(account); // Guardar en base de datos

        // Respuesta exitosa
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Cuenta creada exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // Success
    }


    /**
     * {@inheritDoc}
     * 
     * <p>Valida que el usuario exista, verifica si hay un token válido previo
     * (y devuelve error si no ha expirado), genera un nuevo token y envía el email.
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException si el usuario no existe
     */
    public Map<String, Object> sendPasswordResetEmail(String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<AccountMaster> userOptional = accountMasterRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            logger.warn("❌ [PASSWORD RESET] User not found: {}", email);
            throw new UserNotFoundException("No user found with that email address");
        }

        AccountMaster user = userOptional.get();
        Date now = new Date();

        // Check if there is a valid token
        if (user.getPasswordResetToken() != null && user.getPasswordResetExpiration() != null) {
            if (user.getPasswordResetExpiration().after(now)) {
                // Calculate remaining minutes
                long diffMillis = user.getPasswordResetExpiration().getTime() - now.getTime();
                long minutesLeft = diffMillis / (60 * 1000);

                response.put("success", false);
                response.put("message", "A reset link was already sent recently. Please wait " + minutesLeft + " minutes before trying again.");
                response.put("minutesLeft", minutesLeft);
                return response;
            }
        }

        // Generate new token
        String resetToken = CodeGenerator.generateVerificationCode();
        user.setPasswordResetToken(resetToken);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        user.setPasswordResetExpiration(calendar.getTime());

        accountMasterRepository.save(user);

        String url = "https://l2terra.online/reset-password?token=";
        String resetLink = url + resetToken;
        String subject = "Password Recovery";
        String body = emailContent.buildPasswordResetEmailBody(resetLink, user.getEmail());

        // Enviar email de forma asíncrona
        final String finalEmail = user.getEmail();
        final Long userId = user.getId();
        emailService.sendEmail(user.getEmail(), subject, body)
                .thenRun(() -> {
                })
                .exceptionally(ex -> {
                    logger.error("❌ [PASSWORD RESET] Error enviando email a {}: {}", finalEmail, ex.getMessage());
                    // ROLLBACK: Si el email falla, eliminar el token para permitir nuevo intento
                    try {
                        Optional<AccountMaster> userOpt = accountMasterRepository.findById(userId);
                        if (userOpt.isPresent()) {
                            AccountMaster userToRollback = userOpt.get();
                            // Solo eliminar si el token es el mismo (no fue usado)
                            if (resetToken.equals(userToRollback.getPasswordResetToken())) {
                                userToRollback.setPasswordResetToken(null);
                                userToRollback.setPasswordResetExpiration(null);
                                accountMasterRepository.save(userToRollback);
                                logger.warn("🔄 [PASSWORD RESET] Token eliminado por fallo en envío de email para: {}", finalEmail);
                            }
                        }
                    } catch (Exception rollbackEx) {
                        logger.error("❌ [PASSWORD RESET] Error haciendo rollback del token para {}: {}", finalEmail, rollbackEx.getMessage());
                    }
                    return null;
                });
        
        // Responder inmediatamente (el email se envía en segundo plano)
        response.put("success", true);
        response.put("message", "The password reset link has been sent to your email.");

        response.put("token", resetToken); // Optional, debug
        response.put("expiration", user.getPasswordResetExpiration());

        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Valida que el token exista y no esté expirado, codifica la nueva contraseña
     * y elimina el token de reseteo del usuario.
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.TokenExpiredException si el token es inválido o expiró
     */
    @Override
    @Transactional
    public Map<String, Object> resetPassword(String tokenUser, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        Optional<AccountMaster> userOpt = accountMasterRepository.findByPasswordResetToken(tokenUser);

        if (userOpt.isEmpty()) {
            logger.warn("❌ [RESET PASSWORD] Invalid token: {}", tokenUser);
            throw new TokenExpiredException("Invalid or expired token");
        }

        AccountMaster user = userOpt.get();

        if (user.getPasswordResetExpiration() == null || user.getPasswordResetExpiration().before(new Date())) {
            logger.warn("❌ [RESET PASSWORD] Token expired: {}", tokenUser);
            throw new TokenExpiredException("Token expired. Please request a new one");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiration(null);

        accountMasterRepository.save(user);

        response.put("success", true);
        response.put("message", "Contraseña actualizada con éxito.");
        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Verifica que el usuario exista, que el email no esté verificado,
     * valida el tiempo de espera (5 minutos) y genera un nuevo token de verificación.
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException si el usuario no existe
     */
    @Transactional
    public Map<String, String> resendVerificationEmail(String email) {
        Optional<AccountMaster> optional = accountMasterRepository.findByEmail(email);

        if (optional.isEmpty()) {
            logger.warn("❌ [RESEND VERIFICATION] User not found: {}", email);
            throw new UserNotFoundException("Email not found");
        }

        AccountMaster account = optional.get();

        if (account.isEmailVerified()) {
            return Map.of("status", "error", "message", "Email already verified");
        }

        Date now = new Date();
        if (account.getTokenExpiration() != null && account.getTokenExpiration().after(now)) {
            long millisLeft = account.getTokenExpiration().getTime() - now.getTime();
            long minutes = (millisLeft / 1000) / 60;
            long seconds = (millisLeft / 1000) % 60;

            return Map.of(
                    "status", "forbidden",
                    "message", "Verification email already sent. Try again in " + minutes + "m " + seconds + "s."
            );
        }

        String token = CodeGenerator.generateVerificationCode();
        Date expiration = new Date(now.getTime() + (5 * 60 * 1000)); // 5 minutos de expiración


        account.setVerificationToken(token);
        account.setTokenExpiration(expiration);
        accountMasterRepository.save(account);

        String subject = "Verify your email - L2 Terra";
        String body = emailContent.buildRegistrationVerificationEmailBody(token, email);
        
        // Enviar email de forma asíncrona
        final Long accountId = account.getId();
        final String verificationToken = token;
        emailService.sendEmail(email, subject, body)
                .exceptionally(ex -> {
                    logger.error("❌ [RESEND VERIFICATION] Error enviando email a {}: {}", email, ex.getMessage());
                    // ROLLBACK: Si el email falla, eliminar el token para permitir nuevo intento
                    try {
                        Optional<AccountMaster> accountOpt = accountMasterRepository.findById(accountId);
                        if (accountOpt.isPresent()) {
                            AccountMaster accountToRollback = accountOpt.get();
                            // Solo eliminar si el token es el mismo (no fue usado)
                            if (verificationToken.equals(accountToRollback.getVerificationToken())) {
                                accountToRollback.setVerificationToken(null);
                                accountToRollback.setTokenExpiration(null);
                                accountMasterRepository.save(accountToRollback);
                                logger.warn("🔄 [RESEND VERIFICATION] Token eliminado por fallo en envío de email para: {}", email);
                            }
                        }
                    } catch (Exception rollbackEx) {
                        logger.error("❌ [RESEND VERIFICATION] Error haciendo rollback del token para {}: {}", email, rollbackEx.getMessage());
                    }
                    return null;
                });

        return Map.of("status", "success", "message", "Verification email sent");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Valida que el token exista, que el email no esté ya verificado
     * y que el token no esté expirado. Marca el email como verificado y elimina el token.
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.TokenExpiredException si el token es inválido o expiró
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException si el email ya está verificado
     */
    @Override
    @Transactional
    public ResponseEntity<?> verifyEmail(String token) {
        Optional<AccountMaster> opt = accountMasterRepository.findByVerificationToken(token);

        if (opt.isEmpty()) {
            logger.warn("❌ [VERIFY EMAIL] Invalid token: {}", token);
            throw new TokenExpiredException("Invalid token");
        }

        AccountMaster account = opt.get();

        if (account.isEmailVerified()) {
            logger.warn("❌ [VERIFY EMAIL] Email already verified: {}", account.getEmail());
            throw new EmailNotVerifiedException("Email already verified");
        }

        if (account.getTokenExpiration() == null || account.getTokenExpiration().before(new Date())) {
            logger.warn("❌ [VERIFY EMAIL] Token expired: {}", token);
            throw new TokenExpiredException("Token expired");
        }

        account.setEmailVerified(true);
        account.setVerificationToken(null);
        account.setTokenExpiration(null);
        accountMasterRepository.save(account);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }


    /**
     * Obtiene el email del usuario autenticado desde el contexto de seguridad.
     * 
     * @return Email del usuario autenticado
     */
    public String getEmailFromToken() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Obtiene toda la información del usuario incluyendo roles, terraCoins,
     * estado de verificación y si tiene contraseña (para detectar autenticación OAuth).
     * 
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException si el usuario no existe
     */
    @Override
    public Map<String, Object> getCurrentUser(String email) {
        Optional<AccountMaster> optionalUser = accountMasterRepository.findByEmail(email);
        
        if (optionalUser.isEmpty()) {
            logger.warn("❌ [GET CURRENT USER] User not found: {}", email);
            throw new UserNotFoundException("User not found");
        }

        AccountMaster user = optionalUser.get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("enabled", user.isEnabled());
        response.put("emailVerified", user.isEmailVerified());
        response.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
        response.put("terraCoins", user.getTerraCoins());
        response.put("createdAt", user.getCreatedAt());
        response.put("googleUid", user.getGoogleUid());
        
        // Determinar si el usuario tiene password (si es "oauth_no_password" significa que se creó con Google)
        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().equals("oauth_no_password");
        response.put("hasPassword", hasPassword);
        
        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Cambia la contraseña del usuario. Si el usuario tiene password, valida la contraseña actual.
     * Si no tiene password (OAuth), solo establece la nueva contraseña.
     * 
     * @param email Email del usuario
     * @param currentPassword Contraseña actual (null si el usuario no tiene password)
     * @param newPassword Nueva contraseña
     * @return Map con el resultado (success, message)
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.UserNotFoundException si el usuario no existe
     */
    @Override
    @Transactional
    public Map<String, Object> changePassword(String email, String currentPassword, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<AccountMaster> userOpt = accountMasterRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("❌ [CHANGE PASSWORD] User not found: {}", email);
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }
        
        AccountMaster user = userOpt.get();
        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().equals("oauth_no_password");
        
        // Si el usuario tiene password, validar la contraseña actual
        if (hasPassword) {
            if (currentPassword == null || currentPassword.isBlank()) {
                response.put("success", false);
                response.put("message", "Current password is required");
                return response;
            }
            
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                logger.warn("❌ [CHANGE PASSWORD] Invalid current password for user: {}", email);
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return response;
            }
        }
        
        // Cambiar la contraseña (las validaciones ya se realizan en el DTO)
        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            accountMasterRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Password updated successfully");
        } catch (Exception e) {
            logger.error("❌ [CHANGE PASSWORD] Error changing password for user: {}", email, e);
            response.put("success", false);
            response.put("message", "Error updating password");
        }
        
        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Genera un código de 6 dígitos y lo envía al email del usuario.
     * El código tiene una validez de 5 minutos y se valida que no se haya
     * enviado otro código recientemente.
     */
    @Transactional
    @Override
    public Map<String, String> generateAndSendDeactivationCode(String email) {
        AccountDeactivateCode existing = accountDeactivateCodeRepository.findByEmail(email).orElse(null);

        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (existing != null) {
            Timestamp expire = existing.getDeactivateCodeExpire();

            // Validación: si el código aún no expiró, no enviar otro
            if (expire != null && expire.after(now)) {
                long millisLeft = expire.getTime() - now.getTime();
                long minutes = (millisLeft / 1000) / 60;
                long seconds = (millisLeft / 1000) % 60;

                Map<String, String> map = new HashMap<>();
                map.put("status", "forbidden");
                map.put("message", "Code already sent. Try again in " + minutes + "m " + seconds + "s.");
                return map;
            }
        }

        String code = CodeGenerator.generateSixDigitCode();

        AccountDeactivateCode codeEntry = existing != null ? existing : new AccountDeactivateCode();
        codeEntry.setEmail(email);
        codeEntry.setDeactivateCode(code);
        codeEntry.setDeactivateCodeExpire(new Timestamp(now.getTime() + 5 * 60 * 1000)); // 5 min
        codeEntry.setUsed(false);

        accountDeactivateCodeRepository.save(codeEntry);

        String subject = "Account Deactivation Code - L2 Terra";
        String body = emailContent.buildAccountDeactivationCodeEmailBody(code, email);

        // Enviar email de forma asíncrona
        final String finalEmail = email;
        final String finalDeactivateCode = code;
        emailService.sendEmail(email, subject, body)
                .exceptionally(ex -> {
                    logger.error("❌ [ACCOUNT DEACTIVATE] Error enviando código de desactivación a {}: {}", finalEmail, ex.getMessage());
                    // ROLLBACK: Si el email falla, eliminar el código para permitir nuevo intento
                    try {
                        Optional<AccountDeactivateCode> codeOpt = accountDeactivateCodeRepository.findByEmail(finalEmail);
                        if (codeOpt.isPresent()) {
                            AccountDeactivateCode codeToRollback = codeOpt.get();
                            // Solo eliminar si el código es el mismo (no fue usado)
                            if (finalDeactivateCode.equals(codeToRollback.getDeactivateCode())) {
                                accountDeactivateCodeRepository.delete(codeToRollback);
                                logger.warn("🔄 [ACCOUNT DEACTIVATE] Código de desactivación eliminado por fallo en envío de email para: {}", finalEmail);
                            }
                        }
                    } catch (Exception rollbackEx) {
                        logger.error("❌ [ACCOUNT DEACTIVATE] Error en rollback: {}", rollbackEx.getMessage());
                    }
                    return null;
                });

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Deactivation code sent to your email");
        logger.info("✅ [ACCOUNT DEACTIVATE] Código de desactivación enviado a: {}", email);
        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Verifica el código de desactivación y desactiva la cuenta del usuario.
     * El código debe estar activo, no haber expirado y no haber sido usado previamente.
     * Registra la desactivación en la tabla de auditoría.
     */
    @Transactional
    @Override
    public Map<String, Object> verifyDeactivationCode(String email, String code, String ipAddress) {
        Map<String, Object> response = new HashMap<>();

        Optional<AccountDeactivateCode> codeOpt = accountDeactivateCodeRepository.findByEmail(email);

        if (codeOpt.isEmpty()) {
            logger.warn("❌ [ACCOUNT DEACTIVATE] Código no encontrado para email: {}", email);
            response.put("status", "unauthorized");
            response.put("message", "Invalid deactivation code");
            return response;
        }

        AccountDeactivateCode deactivateCode = codeOpt.get();

        // Verificar que el código no haya sido usado
        if (deactivateCode.isUsed()) {
            logger.warn("❌ [ACCOUNT DEACTIVATE] Código ya usado para email: {}", email);
            response.put("status", "unauthorized");
            response.put("message", "This code has already been used");
            return response;
        }

        // Verificar que el código coincida
        if (code == null || !code.equals(deactivateCode.getDeactivateCode())) {
            logger.warn("❌ [ACCOUNT DEACTIVATE] Código inválido para email: {}", email);
            response.put("status", "unauthorized");
            response.put("message", "Invalid deactivation code");
            return response;
        }

        // Verificar que el código no haya expirado
        Timestamp expire = deactivateCode.getDeactivateCodeExpire();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (expire == null || expire.before(now)) {
            logger.warn("❌ [ACCOUNT DEACTIVATE] Código expirado para email: {}", email);
            response.put("status", "expired");
            response.put("message", "Deactivation code expired. Please request a new one.");
            return response;
        }

        // Buscar el usuario
        Optional<AccountMaster> userOpt = accountMasterRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("❌ [ACCOUNT DEACTIVATE] Usuario no encontrado: {}", email);
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        AccountMaster user = userOpt.get();

        // Desactivar la cuenta
        user.setEnabled(false);
        accountMasterRepository.save(user);

        // Marcar el código como usado
        deactivateCode.setUsed(true);
        accountDeactivateCodeRepository.save(deactivateCode);

        // Registrar la desactivación en la tabla de auditoría
        AccountDeactivation deactivation = new AccountDeactivation();
        deactivation.setAccountMaster(user);
        deactivation.setDeactivationDate(new Timestamp(System.currentTimeMillis()));
        deactivation.setReason("USER"); // Desactivación realizada por el propio usuario
        deactivation.setAdmin(null); // No hay admin porque lo hizo el usuario
        deactivation.setIpAddress(ipAddress != null ? ipAddress : "Unknown");
        deactivation.setNotes("Account deactivated by user via verification code");
        accountDeactivationRepository.save(deactivation);

        response.put("status", "success");
        response.put("message", "Account deactivated successfully");
        response.put("enabled", false);
        logger.info("✅ [ACCOUNT DEACTIVATE] Cuenta desactivada exitosamente para: {} (IP: {})", email, ipAddress);
        return response;
    }

    @Override
    public Map<String, Object> updateNotificationPreferences(String email, Map<String, Boolean> preferences) {
        // TODO: Implementar cuando se agreguen campos de notificaciones a AccountMaster
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification preferences update functionality coming soon");
        return response;
    }

    @Override
    public Map<String, Object> requestStreamerRole(String email) {
        // TODO: Implementar cuando se agregue sistema de solicitudes de roles
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Streamer role request functionality coming soon");
        return response;
    }

}
