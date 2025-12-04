# 🚀 Plan de Mejoras - L2 Terra Online API

Este documento detalla las mejoras identificadas en la API, organizadas por prioridad y categoría.

---

## 🔴 CRÍTICO - Seguridad

### 1. Credenciales y Secretos en Código

**Problema:** Las contraseñas, tokens y secretos están hardcodeados en `application.properties`.

**Ubicación:**
- `src/main/resources/application.properties` (líneas 10, 52-53, 65-66, 85-86, 97)
- Credenciales de BD, JWT secret, contraseñas de email, tokens de Mercado Pago

**Solución:**
```properties
# Usar variables de entorno
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
spring.mail.password=${MAIL_PASSWORD}
mercadopago.access.token=${MP_ACCESS_TOKEN}
```

**Archivo `.env` (NO subir a Git):**
```env
DB_PASSWORD=passTerra123
JWT_SECRET=clave_secreta_super_fuerte_generada_aleatoriamente
MAIL_PASSWORD=Holapton2!
MP_ACCESS_TOKEN=APP_USR-...
```

**Acción:** Agregar `.env` al `.gitignore` y documentar variables de entorno requeridas.

---

### 2. JWT Secret Débil

**Problema:** El JWT secret es predecible y débil:
```properties
jwt.secret=miClaveSecretaSuperSegura1234567890$$$
```

**Solución:**
- Generar una clave secreta aleatoria fuerte (mínimo 256 bits)
- Usar `SecureRandom` para generar la clave
- Almacenar en variable de entorno
- Documentar proceso de generación

**Generación de clave segura:**
```bash
openssl rand -base64 64
```

---

### 3. Inconsistencia en Validación de Contraseñas

**Problema:** Diferentes validaciones según el contexto:
- **Registro:** Mínimo 6 caracteres
- **Cambio/Reset:** Mínimo 8 caracteres + mayúscula + número + especial

**Ubicación:**
- `RegisterRequestDTO.java` - `@Size(min = 6)`
- `ChangePasswordRequestDTO.java` - `@Size(min = 8)` + `@Pattern`
- `ResetPasswordRequestDTO.java` - `@Size(min = 8)` + `@Pattern`

**Solución:** Unificar validación - Mínimo 8 caracteres con complejidad en TODOS los casos.

**Cambios:**
1. Actualizar `RegisterRequestDTO` con validación completa
2. Actualizar `AccountMaster` entity validation
3. Actualizar documentación de API

---

### 4. Falta Rate Limiting en Endpoints Críticos

**Problema:** Endpoints sensibles sin límites claros:
- `/api/auth/register` - Puede usarse para spam
- `/api/auth/resend-verification` - Puede saturar emails
- `/api/auth/resend-reset-email` - Puede saturar emails

**Solución:** Implementar rate limiting específico por endpoint en `RateLimitFilter` o usar `@RateLimiter` annotation.

**Ejemplo:**
```java
@RateLimiter(name = "register", fallbackMethod = "registerFallback")
@PostMapping("/register")
public ResponseEntity<?> register(...) {
    // ...
}
```

---

### 5. Configuración CORS Permisiva

**Problema:** Revisar configuración CORS para evitar orígenes no autorizados.

**Ubicación:** `SecurityConfig.java` - método `corsConfigurationSource()`

**Solución:** Configurar CORS restrictivo solo para dominios permitidos:
```java
configuration.setAllowedOrigins(List.of(
    "https://l2terra.online",
    "http://localhost:4200" // Solo en dev
));
```

---

## 🟠 ALTO - Arquitectura y Código

### 6. Lógica de Negocio en Controladores

**Problema:** Controladores tienen lógica de negocio que debería estar en servicios.

#### 6.1 `AuthController.refreshToken()` (160+ líneas)

**Ubicación:** `src/main/java/com/ak4n1/terra/api/terra_api/auth/controllers/AuthController.java` (líneas 367-527)

**Lógica a mover a `AuthService`:**
- Validación de JWT del refresh token
- Búsqueda y validación del refresh token en BD
- Validación de estado del usuario (enabled, emailVerified)
- Revocación de tokens del usuario
- Rotación de tokens (invalidar viejo, crear nuevos)
- Generación de nuevos access y refresh tokens
- Guardado de tokens en BD

**Solución:**
```java
// En AuthService
@Transactional
public RefreshTokenResponse refreshToken(String refreshToken, HttpServletResponse response) {
    // Toda la lógica actual del controlador
}

// En AuthController
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(...) {
    return authService.refreshToken(refreshToken, response);
}
```

#### 6.2 `AuthController.logout()`

**Ubicación:** Líneas 134-154

**Lógica a mover:**
- Eliminación de tokens de BD
- Limpieza de cookies

**Solución:** Crear `AuthService.logout()`

#### 6.3 `AuthController.getRecentActivity()`

**Ubicación:** Líneas 289-321

**Lógica a mover:**
- Consulta paginada al repositorio
- Mapeo de entidades a DTOs
- Construcción de respuesta paginada

**Solución:** Crear `AuthService.getRecentActivity(email, page, size)`

#### 6.4 `PaymentController.createPaymentPreference()`

**Ubicación:** Líneas 133-200

**Lógica a mover:**
- Validación de packageId
- Obtención del accountId del usuario autenticado
- Generación de URLs (returnUrl, cancelUrl, notificationUrl)

**Solución:** Mover al servicio `PaymentService.createPaymentPreference()`

---

### 7. System.out.println en Producción

**Problema:** Uso de `System.out.println` en lugar de logger.

**Ubicación:**
- `AuthController.java` (líneas 98, 259)
- `StreamerApplicationServiceImpl.java` (líneas 80-88)

**Solución:** Reemplazar todos con `logger.info()`, `logger.debug()`, etc.

**Buscar y reemplazar:**
```bash
# Buscar todos los System.out.println
grep -r "System.out.println" src/
```

---

### 8. Validación Redundante

**Problema:** Validación doble en controladores:
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto, BindingResult result) {
    if (result.hasErrors()) {
        return validation(result); // Redundante
    }
    return authService.save(dto);
}
```

**Ubicación:** `AuthController.java` línea 92

**Solución:** Eliminar validación manual, confiar en `@Valid` y `GlobalExceptionHandler`.

**Cambio:**
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
    return authService.save(dto);
}
```

---

### 9. Duplicación de Endpoints

**Problema:** Dos endpoints hacen lo mismo:
- `/api/auth/me` (líneas 162-183)
- `/api/auth/getme` (líneas 194-207)

**Solución:** Eliminar uno de los dos (preferiblemente `/getme`).

---

### 10. Método Helper Duplicado

**Problema:** `getEmailFromToken()` está duplicado en múltiples controladores.

**Ubicación:**
- `GameAccountController.java` (líneas 127-140)
- También existe en `AuthServiceImpl` (línea 385)

**Solución:** Crear utilidad compartida o usar `@AuthenticationPrincipal`:
```java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal String email) {
    return authService.getCurrentUser(email);
}
```

---

## 🟡 MEDIO - Testing y Calidad

### 11. Falta de Tests

**Problema:** Cobertura de tests extremadamente baja (solo `EmailTest.java` encontrado).

**Solución:** Implementar tests para:

1. **Tests Unitarios:**
   - `AuthService` - registro, login, refresh token
   - `PaymentService` - creación de preferencias
   - `GameAccountService` - creación de cuentas

2. **Tests de Integración:**
   - Endpoints de autenticación
   - Endpoints de pagos
   - Webhooks de Mercado Pago

3. **Tests de Seguridad:**
   - Validación de JWT
   - Rate limiting
   - Autenticación/autorización

**Estructura sugerida:**
```
src/test/java/com/ak4n1/terra/api/terra_api/
├── auth/
│   ├── AuthServiceTest.java
│   ├── AuthControllerTest.java
│   └── JwtTokenTest.java
├── payments/
│   ├── PaymentServiceTest.java
│   └── WebhookControllerTest.java
└── game/
    └── GameAccountServiceTest.java
```

---

### 12. TODOs Sin Implementar

**Problema:** Funcionalidades críticas marcadas como TODO.

**Ubicación:**
- `AuthServiceImpl.java`:
  - Línea 645: `updateNotificationPreferences()` - Solo retorna mensaje
  - Línea 654: `requestStreamerRole()` - Solo retorna mensaje

- `MercadoPagoServiceImpl.java`:
  - Línea 410: Obtención real de información de pago
  - Línea 418: Verificación real del estado
  - Línea 426: Reembolso real

**Solución:** Implementar o documentar por qué no se implementan.

---

### 13. Manejo de Errores Inconsistente

**Problema:** Diferentes formas de manejar errores:
- Algunos métodos retornan `Map<String, Object>`
- Otros retornan `ResponseEntity<?>`
- Algunos lanzan excepciones, otros retornan Maps con error

**Ejemplo:** `AuthServiceImpl.changePassword()` retorna Map en lugar de lanzar excepción.

**Solución:** Estandarizar:
- Los servicios deben lanzar excepciones
- Los controladores deben manejar excepciones vía `GlobalExceptionHandler`
- Las respuestas deben ser consistentes

---

## 🔵 BAJO - Configuración y Mantenibilidad

### 14. Configuración Mezclada de Entornos

**Problema:** Configuración de dev y prod mezclada en `application.properties`.

**Solución:** Separar en perfiles:
- `application-dev.properties` - Solo dev
- `application-prod.properties` - Solo prod
- `application.properties` - Configuración común

**Mejora:** Usar variables de entorno para secrets:
```properties
# application.properties (común)
spring.datasource.url=jdbc:mariadb://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

---

### 15. URLs Hardcodeadas

**Problema:** URLs de ngrok y producción mezcladas.

**Ubicación:** `application.properties` líneas 97-99

**Solución:** Usar variables de entorno por perfil:
```properties
# Dev
mercadopago.notification.url=${NGROK_URL}/api/payments/webhook

# Prod
mercadopago.notification.url=https://l2terra.online/api/payments/webhook
```

---

### 16. Logging Excesivo en Producción

**Problema:** Niveles DEBUG y TRACE activados en producción.

**Ubicación:** `application.properties` líneas 113-134

**Solución:** Configurar por perfil:
```properties
# application-prod.properties
logging.level.root=INFO
logging.level.com.ak4n1.terra.api.terra_api=INFO

# application-dev.properties
logging.level.root=DEBUG
logging.level.com.ak4n1.terra.api.terra_api=DEBUG
```

---

### 17. Falta Documentación API (Swagger/OpenAPI)

**Problema:** No hay documentación interactiva de la API.

**Solución:** Agregar SpringDoc OpenAPI:

**Dependencia:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Configuración:**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI terraApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("L2 Terra Online API")
                .version("1.0.0")
                .description("API REST para L2 Terra Online"));
    }
}
```

**Acceso:** `http://localhost:8080/swagger-ui.html`

---

### 18. Nombres de Variables Poco Descriptivos

**Problema:** Variables con nombres cortos e inexpresivos.

**Ubicación:** `SecurityConfig.java` línea 59:
```java
public SecurityConfig(ActiveTokenRepository t, RefreshTokenRepository rt, ...)
```

**Solución:** Usar nombres descriptivos:
```java
public SecurityConfig(
    ActiveTokenRepository activeTokenRepository,
    RefreshTokenRepository refreshTokenRepository,
    ...
)
```

---

### 19. Falta de Constantes

**Problema:** Valores mágicos repetidos:
- `5 * 60 * 1000` (5 minutos) - Aparece múltiples veces
- `3600000` (1 hora) - Aparece múltiples veces

**Solución:** Crear clase de constantes:
```java
public final class TokenConstants {
    public static final long VERIFICATION_TOKEN_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutos
    public static final long RESET_TOKEN_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutos
    public static final long DEFAULT_TOKEN_EXPIRATION_MS = 3600000; // 1 hora
    
    private TokenConstants() {}
}
```

---

### 20. Base de Datos - ddl-auto=update en Producción

**Problema:** `spring.jpa.hibernate.ddl-auto=update` puede causar cambios no controlados en el esquema.

**Ubicación:** `application.properties` línea 57

**Solución:** 
- Producción: `ddl-auto=validate` o `none`
- Usar migraciones con Flyway o Liquibase para cambios controlados

**Dependencia Flyway:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

## 📋 Checklist de Implementación

### Seguridad (Crítico)
- [ ] Mover todas las credenciales a variables de entorno
- [ ] Generar y configurar JWT secret fuerte
- [ ] Unificar validación de contraseñas (mínimo 8 con complejidad)
- [ ] Implementar rate limiting en endpoints críticos
- [ ] Revisar y ajustar configuración CORS

### Arquitectura (Alto)
- [ ] Mover lógica de `AuthController.refreshToken()` a servicio
- [ ] Mover lógica de `AuthController.logout()` a servicio
- [ ] Mover lógica de `AuthController.getRecentActivity()` a servicio
- [ ] Mover validaciones de `PaymentController` a servicio
- [ ] Eliminar todos los `System.out.println`
- [ ] Eliminar validación redundante en controladores
- [ ] Eliminar endpoint duplicado `/getme`
- [ ] Crear utilidad compartida para `getEmailFromToken()`

### Testing (Medio)
- [ ] Implementar tests unitarios para servicios críticos
- [ ] Implementar tests de integración para endpoints
- [ ] Implementar tests de seguridad
- [ ] Implementar o documentar TODOs

### Configuración (Bajo)
- [ ] Separar configuración por perfiles (dev/prod)
- [ ] Configurar logging por perfil
- [ ] Agregar SpringDoc OpenAPI
- [ ] Refactorizar nombres de variables
- [ ] Crear clase de constantes
- [ ] Configurar Flyway/Liquibase

---

## 🎯 Prioridades

### Sprint 1 (Crítico - Seguridad)
1. Variables de entorno para secrets
2. JWT secret fuerte
3. Unificar validación de contraseñas
4. Rate limiting en endpoints críticos

### Sprint 2 (Alto - Arquitectura)
1. Mover lógica de negocio de controladores a servicios
2. Eliminar `System.out.println`
3. Eliminar validaciones redundantes
4. Refactorizar métodos duplicados

### Sprint 3 (Medio - Testing)
1. Tests unitarios básicos
2. Tests de integración
3. Documentar/implementar TODOs

### Sprint 4 (Bajo - Mejoras)
1. Documentación API (Swagger)
2. Separar configuración por perfiles
3. Refactorizaciones menores

---

## 📚 Referencias

- [Spring Security Best Practices](https://spring.io/guides/topicals/spring-security-architecture)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

**Última actualización:** 2025-01-22

