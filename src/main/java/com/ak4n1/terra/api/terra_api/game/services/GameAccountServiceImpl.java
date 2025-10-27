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

@Service
public class GameAccountServiceImpl implements GameAccountService {

    private static final Logger logger = LoggerFactory.getLogger(GameAccountServiceImpl.class);

    @Autowired
    private EmailNotificationService emailService;

    @Autowired
    private AccountGameRepository accountRepo;

    @Autowired
    private AccountCreateCodeRepository accountCreateCodeRepository;


    @Transactional
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

        StringBuilder body = new StringBuilder();
        body.append("<div style=\"")
                .append("font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64;")
                .append("padding: 40px 20px;")
                .append("text-align: center;")
                .append("\">")
                .append("<img src=\"https://assets.l2terra.online/imgs/lineage2Terra_negro.png\" ")
                .append("alt=\"L2 Terra Logo\" ")
                .append("style=\"max-width: 280px; margin-bottom: 30px;\" />")
                .append("<br>")
                .append("<div style=\"")
                .append("display: inline-block;")
                .append("background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px;")
                .append("border-radius: 12px;")
                .append("max-width: 360px;")
                .append("width: 100%;")
                .append("box-sizing: border-box;")
                .append("box-shadow:0px 0px 30px black;")
                .append("\">")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;\">")
                .append("Your password reset code is:")
                .append("</p>")
                .append("<p style=\"font-size: 34px; font-weight: bold; color: #dabe64; margin: 20px 0;\">")
                .append(code)
                .append("</p>")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;\">")
                .append("Game account: <strong>").append(login).append("</strong>")
                .append("</p>")
                .append("<p style=\"font-size: 14px; margin-bottom: 10px;\">")
                .append("This code expires in 15 minutes.")
                .append("</p>")
                .append("<p style=\"font-size: 12px; opacity: 0.6; margin-bottom: 20px;\">")
                .append("If you did not request this, just ignore this email.")
                .append("</p>")
                .append("<a href=\"https://l2terra.online\" style=\"")
                .append("display: inline-block;")
                .append("padding: 12px 24px;")
                .append("font-weight: 600;")
                .append("font-size: 16px;")
                .append("color: #0c0f20;")
                .append("background: linear-gradient(to right, #746535, #dabe64);")
                .append("border-radius: 8px;")
                .append("text-decoration: none;")
                .append("margin-top: 10px;")
                .append("\">")
                .append("Back to Website")
                .append("</a>")
                .append("</div>")
                .append("</div>");

        emailService.sendEmail(account.getEmail(), subject, body.toString());

        Map<String, String> map = new HashMap<>();
        map.put("status", "success"); // 200
        map.put("message", "Reset code sent to associated email");
        return map;
    }


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

        StringBuilder body = new StringBuilder()
                .append("<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #dabe64; padding: 40px 20px; text-align: center;\">")
                .append("<img src=\"https://assets.l2terra.online/logoJuanLineage_baja_enNegro.webp\" alt=\"L2 Terra Logo\" style=\"max-width: 280px; margin-bottom: 30px;\" />")
                .append("<br>")
                .append("<div style=\"display: inline-block; background: linear-gradient(to bottom, #0c0f20, #000000); padding: 30px; border-radius: 12px; max-width: 360px; width: 100%; box-sizing: border-box;\">")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;color: #dabe64;\">Your code to complete account creation is:</p>")
                .append("<p style=\"font-size: 34px; font-weight: bold; color: #dabe64; margin: 20px 0;\">")
                .append(code)
                .append("</p>")
                .append("<p style=\"font-size: 14px; margin-bottom: 10px;color: #dabe64;\">This code expires in 15 minutes.</p>")
                .append("<p style=\"font-size: 12px; opacity: 0.6; margin-bottom: 20px;color: #dabe64;\">If you didn’t request this, you can ignore the message.</p>")
                .append("<a href=\"https://l2terra.online\" style=\"display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 16px; color: #0c0f20; background: linear-gradient(to right, #746535, #dabe64); border-radius: 8px; text-decoration: none; margin-top: 10px;\">")
                .append("Back to Website")
                .append("</a>")
                .append("</div>")
                .append("</div>");

        emailService.sendEmail(email, subject, body.toString());

        Map<String, String> map = new HashMap<>();
        map.put("status", "success");
        map.put("message", "Create code sent to email");
        return map;
    }

    @Transactional
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
