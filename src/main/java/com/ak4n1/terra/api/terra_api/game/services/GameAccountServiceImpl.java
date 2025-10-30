package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.AccountGameRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.AccountGameResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.ChangePasswordGameDTO;
import com.ak4n1.terra.api.terra_api.game.entities.AccountCreateCode;
import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import com.ak4n1.terra.api.terra_api.game.exceptions.AccountAlreadyExistsException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeAlreadyUsedException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeExpiredException;
import com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeNotFoundException;
import com.ak4n1.terra.api.terra_api.game.exceptions.GameAccountNotFoundException;
import com.ak4n1.terra.api.terra_api.game.exceptions.InvalidCreationCodeException;
import com.ak4n1.terra.api.terra_api.game.repositories.AccountCreateCodeRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.AccountGameRepository;
import com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent;
import com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService;
import com.ak4n1.terra.api.terra_api.utils.CodeGenerator;
import com.ak4n1.terra.api.terra_api.game.utils.L2ClientPasswordEncoder;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de gestión de cuentas de juego.
 * 
 * <p>Este servicio maneja todas las operaciones relacionadas con las cuentas de juego,
 * incluyendo creación, recuperación, cambio de contraseñas y generación de códigos
 * de verificación. Utiliza codificación de contraseñas compatible con L2J.
 * 
 * @see GameAccountService
 * @see AccountGameRepository
 * @see AccountCreateCodeRepository
 * @see com.ak4n1.terra.api.terra_api.game.utils.L2ClientPasswordEncoder
 * @see com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService
 * @see com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent
 * @author ak4n1
 * @since 1.0
 */
@Service
public class GameAccountServiceImpl implements GameAccountService {

    private static final Logger logger = LoggerFactory.getLogger(GameAccountServiceImpl.class);

    @Autowired
    private EmailNotificationService emailService;

    @Autowired
    private AccountGameRepository accountRepo;

    @Autowired
    private AccountCreateCodeRepository accountCreateCodeRepository;

    @Autowired
    private EmailContent emailContent;


    /**
     * {@inheritDoc}
     * 
     * @param dto DTO con los datos de la cuenta (username, password, código de creación)
     * @param email Email del usuario propietario
     * @return Entidad AccountGame creada
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeNotFoundException si el código no existe
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeAlreadyUsedException si el código ya fue usado
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.InvalidCreationCodeException si el código es inválido
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeExpiredException si el código expiró
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.AccountAlreadyExistsException si la cuenta ya existe
     */
    @Transactional
    @Override
    public AccountGame createAccount(AccountGameRequestDTO dto, String email) {
        AccountCreateCode accCode = accountCreateCodeRepository.findByEmail(email)
                .orElseThrow(() -> new CreationCodeNotFoundException("Creation code not found or expired"));

        if (accCode.isUsed()) {
            logger.warn("❌ [CREATE ACCOUNT] Code already used for email: {}", email);
            throw new CreationCodeAlreadyUsedException("This code has already been used");
        }

        if (dto.getCreateCode() == null || !dto.getCreateCode().equals(accCode.getCreateCode())) {
            logger.warn("❌ [CREATE ACCOUNT] Invalid creation code for email: {}", email);
            throw new InvalidCreationCodeException("Invalid creation code");
        }

        Timestamp expire = accCode.getCreateCodeExpire();
        if (expire == null || expire.before(new Timestamp(System.currentTimeMillis()))) {
            logger.warn("❌ [CREATE ACCOUNT] Creation code expired for email: {}", email);
            throw new CreationCodeExpiredException("Creation code expired or invalid");
        }

        boolean exists = accountRepo.existsByLogin(dto.getUsername());
        if (exists) {
            logger.warn("❌ [CREATE ACCOUNT] User already exists: {}", dto.getUsername());
            throw new AccountAlreadyExistsException("User already exists");
        }

        // Delete the creation code record after successful validation
        accountCreateCodeRepository.delete(accCode);

        // Create account
        AccountGame account = new AccountGame();
        account.setEmail(email);
        account.setLogin(dto.getUsername());

        try {
            account.setPassword(L2ClientPasswordEncoder.encodePassword(dto.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("Error encoding password: " + e);
        }

        return accountRepo.save(account);
    }




    /**
     * {@inheritDoc}
     * 
     * @param email Email del usuario
     * @return Lista de DTOs con la información de las cuentas
     */
    @Override
    public List<AccountGameResponseDTO> getAccountsByEmail(String email) {
        List<AccountGame> accounts = accountRepo.findByEmail(email);
        return accounts.stream()
                .map(acc -> new AccountGameResponseDTO(
                        acc.getLogin(),
                        acc.getEmail(),
                        acc.getCreatedTime(),
                        acc.getLastActive(),
                        acc.getLastIP(),
                        acc.getLastServer(),
                        acc.getPcIp()
                ))
                .toList();
    }

    /**
     * {@inheritDoc}
     * 
     * @param login Nombre de login de la cuenta de juego
     * @return Map con el estado de la operación ("success" o "forbidden") y mensaje
     */
    @Override
    public Map<String, String> generateAndSendCode(String login) {
        AccountGame account = accountRepo.findByLogin(login)
                .orElseThrow(() -> new GameAccountNotFoundException("Account not found"));
        
        logger.info("🎮 [GAME ACCOUNT] Generando código de reset para login: {}", login);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expireTime = account.getResetExpire();

        if (expireTime != null && expireTime.after(now)) {
            long millisLeft = expireTime.getTime() - now.getTime();
            long minutes = (millisLeft / 1000) / 60;
            long seconds = (millisLeft / 1000) % 60;

            Map<String, String> map = new HashMap<>();
            map.put("status", "forbidden"); // 403
            map.put("message", "A code was already sent. Try again in " + minutes + "m " + seconds + "s.");
            return map;
        }

        String code = CodeGenerator.generateSixDigitCode();
        account.setResetCode(code);
        account.setResetExpire(new Timestamp(now.getTime() + 15 * 60 * 1000)); // 15 min
        accountRepo.save(account);

        String subject = "Your Code to Reset Game Account Password:: " + login;
        String body = emailContent.buildGameAccountPasswordResetEmailBody(code, login);

        emailService.sendEmail(account.getEmail(), subject, body);

        Map<String, String> map = new HashMap<>();
        map.put("status", "success"); // 200
        map.put("message", "Reset code sent to associated email");
        return map;
    }


    /**
     * {@inheritDoc}
     * 
     * @param email Email del usuario que solicita crear la cuenta
     * @return Map con el estado de la operación ("success" o "forbidden") y mensaje
     */
    @Transactional
    @Override
    public Map<String, String> generateAndSendCreateCode(String email) {
        AccountCreateCode existing = accountCreateCodeRepository.findByEmail(email).orElse(null);

        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (existing != null) {
            Timestamp expire = existing.getCreateCodeExpire();

            // 👇 Validación segura
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

        AccountCreateCode codeEntry = existing != null ? existing : new AccountCreateCode();
        codeEntry.setEmail(email);
        codeEntry.setCreateCode(code);
        codeEntry.setCreateCodeExpire(new Timestamp(now.getTime() + 15 * 60 * 1000)); // 15 min

        accountCreateCodeRepository.save(codeEntry);

        String subject = "Account Creation Code";
        String body = emailContent.buildGameAccountCreationCodeEmailBody(code);

        emailService.sendEmail(email, subject, body);

        Map<String, String> map = new HashMap<>();
        map.put("status", "success");
        map.put("message", "Create code sent to email");
        return map;
    }

    /**
     * {@inheritDoc}
     * 
     * @param dto DTO con login, código de reset y nueva contraseña
     * @return Map con el estado de la operación ("success", "unauthorized", "expired" o "error") y mensaje
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.GameAccountNotFoundException si la cuenta no existe
     */
    @Transactional
    @Override
    public Map<String, String> changePassword(ChangePasswordGameDTO dto) {
        AccountGame account = accountRepo.findByLogin(dto.getLogin())
                .orElseThrow(() -> new GameAccountNotFoundException("Account not found"));

        // Validar código y expiración
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (account.getResetCode() == null || !account.getResetCode().equals(dto.getCode())) {
            return Map.of(
                    "status", "unauthorized",
                    "message", "Código inválido"
            );
        }

        if (account.getResetExpire() == null || account.getResetExpire().before(now)) {
            return Map.of(
                    "status", "expired",
                    "message", "Código expirado"
            );
        }

        try {
            String encodedPass = L2ClientPasswordEncoder.encodePassword(dto.getNewPassword());
            account.setPassword(encodedPass);
            // Limpiamos el código y expiración
            account.setResetCode(null);
            account.setResetExpire(null);
            accountRepo.save(account);
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", "Error codificando la contraseña"
            );
        }

        return Map.of(
                "status", "success",
                "message", "Contraseña actualizada correctamente"
        );
    }



}
