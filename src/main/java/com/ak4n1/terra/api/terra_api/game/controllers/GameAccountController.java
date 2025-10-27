package com.ak4n1.terra.api.terra_api.game.controllers;


import com.ak4n1.terra.api.terra_api.game.dto.AccountGameRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.AccountGameResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.ChangePasswordGameDTO;
import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import com.ak4n1.terra.api.terra_api.game.services.GameAccountService;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game/auth")

public class GameAccountController {

    private static final Logger logger = LoggerFactory.getLogger(GameAccountController.class);

    @Autowired
    private GameAccountService accountService;

    @PostMapping("/registerGameAccount")
    public ResponseEntity<Map<String, Object>> registerGameAccount(@RequestBody AccountGameRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();

        String email = getEmailFromToken();
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.debug("🎮 [GAME ACCOUNT] DTO código: {}", dto.getCreateCode());
        logger.debug("🎮 [GAME ACCOUNT] DTO completo: {}", dto);

        try {
            AccountGame account = accountService.createAccount(dto, email);
            response.put("success", true);
            response.put("message", "Cuenta creada con éxito");
            response.put("account", account);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/create-code")
    public ResponseEntity<Map<String, String>> sendCreateCode() {
        String email = getEmailFromToken();  // Email desde token/session
        Map<String, String> response = accountService.generateAndSendCreateCode(email);

        return switch (response.get("status")) {
            case "success" -> ResponseEntity.ok(response);
            case "unauthorized" -> ResponseEntity.status(401).body(response);
            case "forbidden" -> ResponseEntity.status(403).body(response);
            default -> ResponseEntity.status(500).body(response);
        };
    }



    @GetMapping("/accounts")
    public ResponseEntity<List<AccountGameResponseDTO>> getAccountsByEmail(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();  // 400 si no pasaron email
        }

        List<AccountGameResponseDTO> accounts = accountService.getAccountsByEmail(email);
        if (accounts.isEmpty()) {
            return ResponseEntity.noContent().build();  // 204 si no hay cuentas
        }

        return ResponseEntity.ok(accounts);  // 200 con la lista
    }

    @PostMapping("/reset-code")
    public ResponseEntity<?> sendResetCode(@RequestParam String accountName) {
        Map<String, String> response = accountService.generateAndSendCode(accountName);
        logger.debug("🎮 [GAME ACCOUNT] Response Map: {}", response);

        String status = response.get("status");
        String message = response.get("message");

        Map<String, String> body = new HashMap<>();
        body.put("message", message);

        switch (status) {
            case "success":
                logger.info("🔐 [GAME ACCOUNT] Código generado para {}", accountName);
                return ResponseEntity.ok(body); // 200 OK
            case "unauthorized":
                return ResponseEntity.status(401).body(body); // 401 Unauthorized
            case "forbidden":
                return ResponseEntity.status(403).body(body); // 403 Forbidden
            default:
                return ResponseEntity.badRequest().body(body); // 400 Bad Request
        }

    }

    public String getEmailFromToken() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordGameDTO dto) {
        Map<String, String> result = accountService.changePassword(dto);
        HttpStatus status = switch (result.get("status")) {
            case "success" -> HttpStatus.OK;
            case "unauthorized" -> HttpStatus.UNAUTHORIZED;
            case "expired" -> HttpStatus.FORBIDDEN;
            case "not_found" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return new ResponseEntity<>(result, status);
    }
}
