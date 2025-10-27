package com.ak4n1.terra.api.terra_api.payments.controllers;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPackageResponseDTO;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPurchaseRequest;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.services.CoinService;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de pagos
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private CoinService coinService;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Value("${mercadopago.notification.url}")
    private String notificationUrl;
    
    /**
     * Obtener todos los paquetes de monedas activos
     */
    @GetMapping("/packages")
    public ResponseEntity<List<CoinPackageResponseDTO>> getAllPackages(HttpServletRequest request) {
        logger.info("📦 [PACKAGES] Solicitando todos los paquetes activos");
        logger.info("📦 [PACKAGES] IP Remota: {}", request.getRemoteAddr());
        logger.info("📦 [PACKAGES] User-Agent: {}", request.getHeader("User-Agent"));
        
        try {
            List<CoinPackageResponseDTO> packages = paymentService.getAllActivePackages();
            logger.info("✅ [PACKAGES] Paquetes obtenidos exitosamente: {} paquetes", packages.size());
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            logger.error("❌ [PACKAGES] Error al obtener paquetes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener métodos de pago disponibles
     */
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods() {
        try {
            Map<String, Object> methods = new HashMap<>();
            
            // Métodos de pago disponibles
            List<Map<String, Object>> paymentMethods = List.of(
                Map.of(
                    "id", "mercadopago",
                    "name", "Mercado Pago",
                    "description", "Paga con tarjeta, efectivo o transferencia",
                    "enabled", true,
                    "icon", "credit-card",
                    "supportedTypes", List.of("credit_card", "debit_card", "cash", "transfer")
                )
            );
            
            methods.put("methods", paymentMethods);
            methods.put("defaultMethod", "mercadopago");
            methods.put("currency", "ARS");
            methods.put("country", "AR");
            
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            logger.error("Error al obtener métodos de pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener paquetes populares
     */
    @GetMapping("/packages/popular")
    public ResponseEntity<List<CoinPackageResponseDTO>> getPopularPackages() {
        try {
            List<CoinPackageResponseDTO> packages = paymentService.getPopularPackages();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            logger.error("Error al obtener paquetes populares: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener un paquete específico por ID
     */
    @GetMapping("/packages/{id}")
    public ResponseEntity<CoinPackageResponseDTO> getPackageById(@PathVariable Long id) {
        try {
            CoinPackageResponseDTO packageDTO = paymentService.getPackageById(id);
            if (packageDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            logger.error("Error al obtener paquete por ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crear preferencia de pago para Mercado Pago
     */
    @PostMapping("/create-preference")
    public ResponseEntity<PaymentPreferenceResponse> createPaymentPreference(
            @RequestBody CoinPurchaseRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("🔵 [PAYMENT] Iniciando creación de preferencia de pago");
        logger.info("🔵 [PAYMENT] Request recibido: packageId={}, accountId={}", 
                   request.getPackageId(), request.getAccountId());
        logger.info("🔵 [PAYMENT] Headers: User-Agent={}, X-Forwarded-For={}", 
                   httpRequest.getHeader("User-Agent"), httpRequest.getHeader("X-Forwarded-For"));
        
        try {
            // Validar que se proporcione el packageId
            if (request.getPackageId() == null) {
                logger.warn("❌ [PAYMENT] PackageId no proporcionado");
                return ResponseEntity.badRequest()
                        .body(new PaymentPreferenceResponse("error", "El ID del paquete es obligatorio"));
            }
            
            // Obtener el accountId del usuario autenticado si no se proporciona
            if (request.getAccountId() == null) {
                logger.info("🔵 [PAYMENT] AccountId no proporcionado, buscando usuario autenticado");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    logger.info("🔵 [PAYMENT] Usuario autenticado: {}", auth.getName());
                    // Buscar la cuenta por email del usuario autenticado
                    AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
                    request.setAccountId(account.getId());
                    logger.info("🔵 [PAYMENT] AccountId asignado: {}", account.getId());
                } else {
                    logger.warn("❌ [PAYMENT] Usuario no autenticado");
                    return ResponseEntity.badRequest()
                            .body(new PaymentPreferenceResponse("error", "Usuario no autenticado"));
                }
            }
            
            // Generar URLs de retorno y asignarlas al request
            String returnUrl = getReturnUrl(httpRequest);
            String cancelUrl = getCancelUrl(httpRequest);
            String notificationUrl = getNotificationUrl(httpRequest);
            
            request.setReturnUrl(returnUrl);
            request.setCancelUrl(cancelUrl);
            request.setNotificationUrl(notificationUrl);
            
            logger.info("🔵 [PAYMENT] URLs generadas - Return: {}, Cancel: {}, Notification: {}", 
                       returnUrl, cancelUrl, notificationUrl);
            
            logger.info("🔵 [PAYMENT] Llamando a paymentService.createPaymentPreference");
            PaymentPreferenceResponse response = paymentService.createPaymentPreference(request);
            
            logger.info("🔵 [PAYMENT] Respuesta del servicio: status={}, preferenceId={}", 
                       response.getStatus(), response.getPreferenceId());
            
            if ("error".equals(response.getStatus())) {
                logger.error("❌ [PAYMENT] Error en la respuesta del servicio: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            logger.info("✅ [PAYMENT] Preferencia creada exitosamente: {}", response.getPreferenceId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [PAYMENT] Error al crear preferencia de pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentPreferenceResponse("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Obtener historial de transacciones de una cuenta
     */
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionHistory(@PathVariable Long accountId) {
        try {
            List<PaymentTransaction> transactions = paymentService.getAccountTransactionHistory(accountId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error al obtener historial de transacciones de la cuenta {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener estadísticas de pagos de una cuenta
     */
    @GetMapping("/stats/{accountId}")
    public ResponseEntity<CoinService.CoinAccountStats> getAccountStats(@PathVariable Long accountId) {
        try {
            CoinService.CoinAccountStats stats = paymentService.getAccountPaymentStats(accountId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de la cuenta {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener balance de monedas de una cuenta
     */
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable Long accountId) {
        try {
            Integer balance = coinService.getAccountCoinsBalance(accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("accountId", accountId);
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener balance de la cuenta {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Verificar estado de una transacción
     */
    @GetMapping("/transaction/{transactionId}/status")
    public ResponseEntity<Map<String, String>> getTransactionStatus(@PathVariable Long transactionId) {
        try {
            String status = paymentService.getTransactionStatus(transactionId);
            Map<String, String> response = new HashMap<>();
            response.put("transactionId", transactionId.toString());
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener estado de transacción {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Reembolsar una transacción (solo administradores)
     */
    @PostMapping("/transaction/{transactionId}/refund")
    public ResponseEntity<Map<String, Object>> refundTransaction(
            @PathVariable Long transactionId,
            @RequestParam String reason) {
        try {
            boolean success = paymentService.refundTransaction(transactionId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transactionId);
            response.put("success", success);
            response.put("reason", reason);
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error al reembolsar transacción {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Agregar monedas manualmente (solo administradores)
     */
    @PostMapping("/admin/add-coins")
    public ResponseEntity<Map<String, Object>> addCoinsManually(
            @RequestParam Long accountId,
            @RequestParam Integer coinsAmount,
            @RequestParam String reason) {
        try {
            coinService.addCoinsToAccount(accountId, coinsAmount, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("accountId", accountId);
            response.put("coinsAdded", coinsAmount);
            response.put("reason", reason);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al agregar monedas manualmente a la cuenta {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generar URL de notificación para webhooks
     */
    private String getNotificationUrl(HttpServletRequest request) {
        // Usar la configuración de application.properties
        return notificationUrl;
    }
    
    /**
     * Generar URL de retorno exitoso
     */
    private String getReturnUrl(HttpServletRequest request) {
        // Usar URLs de producción para redirecciones
        return "https://l2terra.online/payment-success";
    }
    
    /**
     * Generar URL de cancelación
     */
    private String getCancelUrl(HttpServletRequest request) {
        // Usar URLs de producción para redirecciones
        return "https://l2terra.online/payment-failure";
    }


}
