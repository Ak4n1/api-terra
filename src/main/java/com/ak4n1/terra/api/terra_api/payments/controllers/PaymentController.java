package com.ak4n1.terra.api.terra_api.payments.controllers;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.payments.exceptions.PackageNotFoundException;
import com.ak4n1.terra.api.terra_api.payments.exceptions.PaymentException;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPackageResponseDTO;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPurchaseRequest;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentTransactionDTO;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;
import com.ak4n1.terra.api.terra_api.payments.factory.PaymentStrategyFactory;
import com.ak4n1.terra.api.terra_api.payments.repositories.PaymentTransactionRepository;
import com.ak4n1.terra.api.terra_api.payments.strategies.PaymentStrategy;
import com.ak4n1.terra.api.terra_api.payments.services.CoinService;
import com.ak4n1.terra.api.terra_api.payments.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de pagos y transacciones.
 * 
 * <p>Este controlador expone endpoints para:
 * <ul>
 *   <li>Consultar paquetes de monedas disponibles</li>
 *   <li>Crear preferencias de pago (Mercado Pago y PayPal)</li>
 *   <li>Capturar pagos de PayPal</li>
 *   <li>Consultar historial de transacciones</li>
 *   <li>Obtener estadísticas y balance de cuenta</li>
 *   <li>Reanudar pagos pendientes</li>
 *   <li>Reembolsar transacciones (administradores)</li>
 * </ul>
 * 
 * <p><b>Seguridad:</b>
 * <ul>
 *   <li>Endpoints protegidos requieren autenticación</li>
 *   <li>Validación de accountId: usuarios solo acceden a sus propios datos</li>
 *   <li>Rate limiting: máximo 30 intentos de pago por hora</li>
 *   <li>Límite diario: máximo 20 compras exitosas por día</li>
 * </ul>
 * 
 * <p><b>Nota:</b> CORS manejado por SecurityConfig (no usar @CrossOrigin aquí)
 * 
 * @author ak4n1
 * @since 3.0
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private CoinService coinService;
    
    @Autowired
    private AccountMasterRepository accountMasterRepository;
    
    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Value("${mercadopago.notification.url}")
    private String mercadoPagoNotificationUrl;
    
    @Value("${paypal.webhook.url}")
    private String paypalNotificationUrl;
    
    @Value("${mercadopago.return.url}")
    private String paymentReturnUrl;
    
    @Value("${mercadopago.cancel.url}")
    private String paymentCancelUrl;
    
    /**
     * Obtiene todos los paquetes de monedas activos.
     * 
     * <p>Este endpoint permite filtrar los paquetes por moneda si se proporciona
     * el parámetro currency. Si no se proporciona, retorna todos los paquetes activos.
     * 
     * @param currency Filtro opcional por moneda (USD, ARS). Si es null, retorna todos los paquetes
     * @param request Objeto HttpServletRequest para obtener información del cliente
     * @return Lista de paquetes de monedas activos, o lista vacía si no hay paquetes
     */
    @GetMapping("/packages")
    public ResponseEntity<List<CoinPackageResponseDTO>> getAllPackages(
            @RequestParam(required = false) String currency,
            HttpServletRequest request) {
        logger.info("📦 [PACKAGES] Solicitando paquetes activos - Currency: {}", currency);
        logger.info("📦 [PACKAGES] IP Remota: {}", request.getRemoteAddr());
        
        try {
            List<CoinPackageResponseDTO> packages;
            if (currency != null && !currency.isEmpty()) {
                packages = paymentService.getActivePackagesByCurrency(currency.toUpperCase());
            } else {
                packages = paymentService.getAllActivePackages();
            }
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            logger.error("❌ [PACKAGES] Error al obtener paquetes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene los métodos de pago disponibles en el sistema.
     * 
     * <p>Retorna información sobre los proveedores de pago configurados
     * (Mercado Pago y PayPal) con sus características y tipos de pago soportados.
     * 
     * @return Mapa con información de métodos de pago, método por defecto, moneda y país
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
                    "description", "Secure payment with Mercado Pago",
                    "enabled", true,
                    "icon", "credit-card",
                    "supportedTypes", List.of("credit_card", "debit_card", "cash", "transfer")
                ),
                Map.of(
                    "id", "paypal",
                    "name", "PayPal",
                    "description", "Pay securely with PayPal",
                    "enabled", true,
                    "icon", "paypal",
                    "supportedTypes", List.of("paypal", "credit_card", "debit_card")
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
     * Obtiene los paquetes de monedas más populares.
     * 
     * <p>Los paquetes populares se determinan según criterios internos del sistema
     * (por ejemplo, número de compras, orden de visualización, etc.).
     * 
     * @return Lista de paquetes populares
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
     * Obtiene un paquete de monedas específico por su ID.
     * 
     * @param id El ID del paquete a obtener
     * @return El paquete de monedas si existe, o 404 si no se encuentra
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
     * Crea una preferencia de pago para un paquete de monedas.
     * 
     * <p>Este endpoint crea una preferencia de pago en el proveedor seleccionado
     * (Mercado Pago o PayPal) y retorna la URL para que el usuario complete el pago.
     * 
     * <p><b>Validaciones de seguridad:</b>
     * <ul>
     *   <li>Usuario debe estar autenticado</li>
     *   <li>Email del usuario debe estar verificado</li>
     *   <li>Paquete debe existir y estar activo</li>
     *   <li>Precio del paquete debe ser válido (positivo y dentro del rango permitido)</li>
     *   <li>Rate limit: máximo 30 intentos por hora por usuario</li>
     *   <li>Límite diario: máximo 20 compras exitosas por día</li>
     * </ul>
     * 
     * <p><b>Proveedores soportados:</b>
     * <ul>
     *   <li>mercadopago (o mp)</li>
     *   <li>paypal (o pp)</li>
     * </ul>
     * 
     * @param request Objeto con los datos de la compra (packageId, accountId, provider)
     * @param httpRequest Objeto HttpServletRequest para obtener información del cliente
     * @return Respuesta con el ID de preferencia/orden y URL de pago, o error si falla
     */
    @PostMapping("/create-preference")
    public ResponseEntity<PaymentPreferenceResponse> createPaymentPreference(
            @RequestBody @Valid CoinPurchaseRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("🔵 [PAYMENT] Iniciando creacion de preferencia de pago");
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
            String provider = request.getProvider() != null ? request.getProvider() : "mercadopago";
            String notificationUrl = getNotificationUrl(provider);
            String ipAddress = getClientIp(httpRequest);
            
            request.setReturnUrl(returnUrl);
            request.setCancelUrl(cancelUrl);
            request.setNotificationUrl(notificationUrl);
            request.setIpAddress(ipAddress);
            
            logger.info("🔵 [PAYMENT] URLs generadas - Return: {}, Cancel: {}, Notification: {}", 
                       returnUrl, cancelUrl, notificationUrl);
            
            // SECURITY: Verificar rate limit (máximo 30 intentos por hora)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                long recentAttempts = countRecentPaymentAttempts(auth.getName());
                if (recentAttempts >= 30) {
                    logger.warn("🚨 [PAYMENT] Rate limit exceeded for user: {}", auth.getName());
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(new PaymentPreferenceResponse("error", "Too many payment attempts. Please try again later."));
                }
                
                // SECURITY: Verificar límite de compras exitosas diarias (máximo 20)
                long approvedToday = countApprovedToday(auth.getName());
                if (approvedToday >= 20) {
                    logger.warn("🚨 [PAYMENT] Daily purchase limit exceeded for user: {}", auth.getName());
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(new PaymentPreferenceResponse("error", "Daily purchase limit reached (20 max). Please try again tomorrow."));
                }
            }
            
            logger.info("🔵 [PAYMENT] Llamando a paymentService.createPaymentPreference");
            PaymentPreferenceResponse response = paymentService.createPaymentPreference(request);
            
            logger.info("🔵 [PAYMENT] Respuesta del servicio: status={}, preferenceId={}", 
                       response.getStatus(), response.getPreferenceId());
            
            if ("error".equals(response.getStatus())) {
                logger.error("❌ [PAYMENT] Error en la respuesta del servicio: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException | PackageNotFoundException | PaymentException | IllegalStateException | SecurityException e) {
            // Dejar que PaymentExceptionHandler maneje estas excepciones
            throw e;
        } catch (Exception e) {
            logger.error("❌ [PAYMENT] Error inesperado al crear preferencia de pago: {}", e.getMessage(), e);
            // Dejar que PaymentExceptionHandler maneje excepciones genéricas
            throw new PaymentException("Unexpected error creating payment preference", e);
        }
    }
    
    /**
     * Capturar orden de PayPal (requiere autenticación)
     * CRITICAL: ACID transaction with rollback on any error
     */
    @PostMapping("/paypal/capture/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> capturePayPalOrder(@PathVariable String orderId) {
        logger.info("💰 [PAYPAL-CAPTURE] Capturando orden de PayPal: {}", orderId);
        
        try {
            // Obtener la estrategia de PayPal
            PaymentStrategy paypalStrategy = paymentStrategyFactory.getPaymentStrategy("paypal");
            
            // Capturar el pago
            PaymentTransaction transaction = paypalStrategy.capturePayment(orderId);
            
            logger.info("✅ [PAYPAL-CAPTURE] Orden capturada exitosamente. Transaction ID: {}", transaction.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment captured successfully");
            response.put("transactionId", transaction.getId());
            response.put("coinsAdded", transaction.getCoinsAmount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [PAYPAL-CAPTURE] Error capturando orden: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error capturing payment: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener historial de transacciones del usuario autenticado (sin paginación - deprecated)
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentTransactionDTO>> getTransactionHistory() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
            
            List<PaymentTransactionDTO> transactions = paymentService.getAccountTransactionHistory(account.getId());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error al obtener historial de transacciones: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener historial de transacciones del usuario autenticado con paginación
     */
    @GetMapping("/history/paginated")
    public ResponseEntity<Map<String, Object>> getTransactionHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
            
            Page<PaymentTransactionDTO> transactionPage = paymentService.getAccountTransactionHistoryPaginated(account.getId(), page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", transactionPage.getContent());
            response.put("totalElements", transactionPage.getTotalElements());
            response.put("totalPages", transactionPage.getTotalPages());
            response.put("currentPage", transactionPage.getNumber());
            response.put("size", transactionPage.getSize());
            response.put("hasNext", transactionPage.hasNext());
            response.put("hasPrevious", transactionPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener historial paginado de transacciones: {}", e.getMessage(), e);
            e.printStackTrace(); // Para debugging
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error loading transaction history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Obtener estadísticas de pagos de una cuenta
     */
    @GetMapping("/stats/{accountId}")
    public ResponseEntity<CoinService.CoinAccountStats> getAccountStats(@PathVariable Long accountId) {
        try {
            // Validar que el accountId pertenezca al usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                        .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
                
                if (!account.getId().equals(accountId)) {
                    logger.warn("Intento de acceso no autorizado: usuario {} intento acceder a stats de cuenta {}", 
                              auth.getName(), accountId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
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
            // Validar que el accountId pertenezca al usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                        .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
                
                if (!account.getId().equals(accountId)) {
                    logger.warn("Intento de acceso no autorizado: usuario {} intento acceder a balance de cuenta {}", 
                              auth.getName(), accountId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
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
            logger.error("Error al obtener estado de transaccion {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener URL para reanudar un pago pendiente
     */
    @GetMapping("/transaction/{transactionId}/resume")
    public ResponseEntity<Map<String, Object>> getResumePaymentUrl(@PathVariable Long transactionId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            AccountMaster account = accountMasterRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para el usuario autenticado"));
            
            String paymentUrl = paymentService.getResumePaymentUrl(transactionId, account.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transactionId);
            response.put("paymentUrl", paymentUrl);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al reanudar pago: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error al obtener URL de reanudacion para transaccion {}: {}", transactionId, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error al reanudar el pago. Intente nuevamente.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            logger.error("Error al reembolsar transaccion {}: {}", transactionId, e.getMessage(), e);
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
    private String getNotificationUrl(String provider) {
        // Retornar la URL correcta según el proveedor
        if ("paypal".equalsIgnoreCase(provider)) {
            return paypalNotificationUrl;
        } else {
            return mercadoPagoNotificationUrl;
        }
    }
    
    /**
     * Obtener IP del cliente considerando proxies (Nginx, Cloudflare, etc.)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs (X-Forwarded-For), tomar la primera (cliente real)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * Generar URL de retorno exitoso
     */
    private String getReturnUrl(HttpServletRequest request) {
        return paymentReturnUrl;
    }
    
    /**
     * Generar URL de cancelación
     */
    private String getCancelUrl(HttpServletRequest request) {
        return paymentCancelUrl;
    }
    
    /**
     * Contar intentos de pago recientes (última hora) - Optimizado con query directo
     */
    private long countRecentPaymentAttempts(String email) {
        try {
            AccountMaster account = accountMasterRepository.findByEmail(email).orElse(null);
            if (account == null) return 0;
            
            Date oneHourAgo = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
            return paymentTransactionRepository.countRecentAttemptsByAccount(account, oneHourAgo);
        } catch (Exception e) {
            logger.error("Error counting recent attempts: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Contar compras exitosas hoy - Optimizado con query directo
     */
    private long countApprovedToday(String email) {
        try {
            AccountMaster account = accountMasterRepository.findByEmail(email).orElse(null);
            if (account == null) return 0;
            
            // Inicio del día actual
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            Date startOfDay = cal.getTime();
            
            return paymentTransactionRepository.countApprovedSince(account, startOfDay);
        } catch (Exception e) {
            logger.error("Error counting approved today: {}", e.getMessage());
            return 0;
        }
    }


}
