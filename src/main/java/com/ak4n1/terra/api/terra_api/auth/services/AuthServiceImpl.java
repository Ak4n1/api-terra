package com.ak4n1.terra.api.terra_api.auth.services;

import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import com.ak4n1.terra.api.terra_api.auth.dto.RegisterResponseDTO;
import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.entities.Role;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.RoleRepository;

import com.ak4n1.terra.api.terra_api.utils.CodeGenerator;
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
        calendar.add(Calendar.MINUTE, 15); // 15 minutos
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

        // Enviar email de verificación
        String subject = "Verify your email - L2 Terra";
        String body = emailContent.buildRegistrationVerificationEmailBody(verificationCode, account.getEmail());
        
        emailService.sendEmail(account.getEmail(), subject, body);


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
        calendar.add(Calendar.MINUTE, 15);
        user.setPasswordResetExpiration(calendar.getTime());

        accountMasterRepository.save(user);

        String url = "https://l2terra.online/reset-password?token=";
        String resetLink = url + resetToken;
        logger.debug("🔗 [PASSWORD RESET] Link generado: {}", resetLink);
        String subject = "Password Recovery";
        String body = emailContent.buildPasswordResetEmailBody(resetLink, user.getEmail());

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            response.put("success", true);
            response.put("message", "The password reset link has been sent to your email.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "There was an error sending the email. Please try again later.");
            response.put("error", e.getMessage());
            return response;
        }

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
     * valida el tiempo de espera (15 minutos) y genera un nuevo token de verificación.
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
        Date expiration = new Date(now.getTime() + (15 * 60 * 1000)); // 15 minutos de expiración


        account.setVerificationToken(token);
        account.setTokenExpiration(expiration);
        accountMasterRepository.save(account);

        String subject = "Verify your email - L2 Terra";
        String body = emailContent.buildRegistrationVerificationEmailBody(token, email);
        
        emailService.sendEmail(email, subject, body);

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

}
