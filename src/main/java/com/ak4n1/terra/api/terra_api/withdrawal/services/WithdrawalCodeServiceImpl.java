package com.ak4n1.terra.api.terra_api.withdrawal.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.notifications.builders.EmailContent;
import com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService;
import com.ak4n1.terra.api.terra_api.utils.CodeGenerator;
import com.ak4n1.terra.api.terra_api.withdrawal.entities.WithdrawalCode;
import com.ak4n1.terra.api.terra_api.withdrawal.repositories.WithdrawalCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de códigos de retiro.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
public class WithdrawalCodeServiceImpl implements WithdrawalCodeService {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalCodeServiceImpl.class);
    
    // 10 minutos en milisegundos
    private static final long CODE_EXPIRATION_MS = 10 * 60 * 1000;
    
    // Cooldown para evitar spam (1 minuto)
    private static final long COOLDOWN_MS = 60 * 1000;

    @Autowired
    private WithdrawalCodeRepository withdrawalCodeRepository;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private EmailContent emailContent;

    @Override
    @Transactional
    public Map<String, Object> generateAndSendCode(String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Buscar el usuario
            Optional<AccountMaster> accountOpt = accountMasterRepository.findByEmail(email);
            if (accountOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "User not found");
                response.put("errorCode", "USER_NOT_FOUND");
                return response;
            }
            
            AccountMaster account = accountOpt.get();
            
            // ⚠️ VALIDACIÓN: Verificar que tenga Terra Coins
            if (account.getTerraCoins() == null || account.getTerraCoins() <= 0) {
                response.put("status", "error");
                response.put("message", "You don't have any Terra Coins to withdraw");
                response.put("errorCode", "ZERO_BALANCE");
                response.put("balance", 0);
                logger.info("[WITHDRAWAL] User {} attempted to generate code with 0 balance", email);
                return response;
            }
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            // Verificar si hay un código válido existente (solo 1 código válido a la vez)
            Optional<WithdrawalCode> existingCode = withdrawalCodeRepository.findValidCodeByEmail(email, now);
            if (existingCode.isPresent()) {
                WithdrawalCode code = existingCode.get();
                long timeUntilExpiration = code.getExpiresAt().getTime() - now.getTime();
                long remainingSeconds = timeUntilExpiration / 1000;
                long remainingMinutes = remainingSeconds / 60;
                long remainingSecs = remainingSeconds % 60;
                
                String timeStr = remainingMinutes > 0 
                    ? remainingMinutes + " min " + remainingSecs + " sec"
                    : remainingSecs + " seconds";
                
                response.put("status", "error");
                response.put("message", "You already have a valid code. It expires in " + timeStr + ". Check your email or wait for it to expire.");
                response.put("errorCode", "CODE_ALREADY_EXISTS");
                response.put("remainingSeconds", remainingSeconds);
                response.put("expiresAt", code.getExpiresAt().toString());
                logger.info("[WITHDRAWAL] User {} already has a valid code, expires in {} seconds", email, remainingSeconds);
                return response;
            }
            
            // Generar nuevo código de 6 dígitos
            String code = CodeGenerator.generateSixDigitCode();
            
            // Calcular expiración (5 minutos)
            Timestamp expiresAt = new Timestamp(now.getTime() + CODE_EXPIRATION_MS);
            
            // Crear y guardar el código
            WithdrawalCode withdrawalCode = new WithdrawalCode(email, code, now, expiresAt);
            withdrawalCodeRepository.save(withdrawalCode);
            
            // Enviar email
            String emailBody = emailContent.buildWithdrawalCodeEmailBody(code, email);
            emailNotificationService.sendEmail(email, "Terra Coins Withdrawal Code", emailBody);
            
            logger.info("[WITHDRAWAL] Code sent to {} - expires at {}", email, expiresAt);
            
            response.put("status", "success");
            response.put("message", "Withdrawal code sent to your email!");
            response.put("expiresAt", expiresAt.toString());
            response.put("expiresInMinutes", 10);
            
        } catch (Exception e) {
            logger.error("[WITHDRAWAL] Error generating code for {}: {}", email, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "An error occurred. Please try again.");
            response.put("errorCode", "INTERNAL_ERROR");
        }
        
        return response;
    }

    @Override
    public boolean validateCode(String email, String code) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Optional<WithdrawalCode> validCode = withdrawalCodeRepository.findValidCode(email, code, now);
        return validCode.isPresent();
    }

    @Override
    @Transactional
    public boolean markCodeAsUsed(String email, String code) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Optional<WithdrawalCode> validCode = withdrawalCodeRepository.findValidCode(email, code, now);
        
        if (validCode.isPresent()) {
            WithdrawalCode withdrawalCode = validCode.get();
            withdrawalCode.setUsed(true);
            withdrawalCode.setUsedAt(now);
            withdrawalCodeRepository.save(withdrawalCode);
            logger.info("[WITHDRAWAL] Code marked as used for {}", email);
            return true;
        }
        
        return false;
    }
}

