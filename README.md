# L2 Terra Online API

API monolítica para el servidor de Lineage 2 Terra Online construida con Spring Boot.

##  Descripción

API REST que proporciona funcionalidades de autenticación, gestión de cuentas de juego, rankings, mercado offline, almacenamiento de jugadores y pagos mediante Mercado Pago. La API se conecta a una base de datos MariaDB del servidor de juego para lectura de datos críticos como personajes, clanes e items.

##  Arquitectura

### Estructura de Paquetes

```
com.ak4n1.terra.api.terra_api/
│
├── auth/                          # Autenticación y Autorización
│   ├── controllers/               # Endpoints de autenticación
│   ├── dto/                      # Data Transfer Objects
│   ├── entities/                 # AccountMaster, ActiveToken, RecentActivity, Role
│   ├── exceptions/               # Excepciones específicas de auth
│   ├── interceptors/             # ActivityInterceptor para logging de actividad
│   ├── repositories/             # Repositorios JPA
│   └── services/                 # Lógica de negocio de autenticación
│
├── game/                         # Funcionalidades del Juego
│   ├── controllers/              # Endpoints de personajes, clanes, rankings, etc.
│   ├── dto/                      # Data Transfer Objects del juego
│   ├── entities/                 # Entidades del juego (Character, Clan, Item, etc.)
│   ├── exceptions/               # Excepciones específicas del juego
│   ├── repositories/             # Repositorios JPA del juego
│   ├── services/                 # Lógica de negocio del juego
│   └── utils/                    # Utilidades del juego (L2ClientPasswordEncoder, ItemParser, etc.)
│
├── payments/                     # Integración con Mercado Pago
│   ├── config/                   # Configuración de Mercado Pago
│   ├── controllers/              # Endpoints de pagos y webhooks
│   ├── dto/                      # Data Transfer Objects de pagos
│   ├── entities/                 # CoinPackage, PaymentTransaction, PaymentStatus
│   ├── repositories/             # Repositorios de pagos
│   └── services/                 # Lógica de negocio de pagos
│
├── security/                     # Configuración de Seguridad
│   ├── config/                   # SecurityConfig, TokenJwtConfig
│   ├── filters/                  # JWT filters (Authentication, Validation)
│   └── services/                 # JpaUserDetailsService
│
├── notifications/                # Sistema de Notificaciones
│   ├── builders/                 # EmailContent (templates HTML)
│   ├── config/                   # EmailConfig
│   └── services/                 # Servicios de notificación (email)
│
├── exceptions/                   # Manejo Global de Excepciones
│   └── GlobalExceptionHandler    # @RestControllerAdvice
│
└── utils/                        # Utilidades Generales
    ├── CodeGenerator             # Generador de códigos
    └── CachedBodyHttpServletRequest
```

##  Seguridad

- **JWT Authentication**: Tokens JWT para autenticación de usuarios
- **Spring Security**: Control de acceso basado en roles (ROLE_USER, ROLE_ADMIN, ROLE_STREAMER)
- **Google OAuth**: Autenticación con Firebase
- **Email Verification**: Verificación de correo electrónico obligatoria
- **Password Reset**: Sistema de recuperación de contraseña
- **Recent Activity**: Logging de acciones del usuario para auditoría

##  Funcionalidades del Juego

### Gestión de Personajes
- Consulta de personajes por email o ID
- Estadísticas de personajes
- Gestión de almacenamiento (Warehouse)
- Sistema de paginación para listados

### Clanes
- Información de clanes
- Miembros y jerarquías
- Estadísticas y rankings

### Rankings
- Top PvP (Player vs Player)
- Top PK (Player Killer)
- Top Clanes
- Estadísticas del servidor

### Mercado Offline
- Consulta de tiendas offline
- Detalles de items en venta
- Búsqueda por personaje

### Items
- Carga de datos de items desde XML
- Caché de items en memoria
- API de consulta de items por ID
- Información detallada de atributos y estadísticas

### Patch Notes
- Sistema de noticias/parches del servidor
- Versiones y actualizaciones

##  Sistema de Pagos (Mercado Pago)

- **Compra de Monedas**: Paquetes de Terra Coins predefinidos
- **Webhooks**: Confirmación automática de pagos
- **Transacciones**: Registro completo de transacciones
- **Sandbox/Producción**: Soporte para ambiente de pruebas y producción

##  Entidades Inmutables (Game)

Algunas entidades del paquete `game` están marcadas como **inmutables** (`@Immutable`) para proteger la integridad de los datos del servidor de juego:

- **Character**: Tabla `characters` - Información de personajes
- **Clan**: Tabla `clan_data` - Información de clanes
- **Item**: Tabla `items` - Información de items del juego

**¿Por qué inmutables?**
Estas entidades mapean tablas que son **exclusivamente manejadas por el servidor de juego** de Lineage 2. Cualquier modificación accidental desde la API podría:
- Corromper el estado de los personajes
- Causar inconsistencias en el juego
- Generar bugs graves
- Afectar el gameplay de los jugadores

Por esta razón, la API **solo lee** de estas tablas usando repositorios con queries de solo lectura para generar rankings, consultar información, y exponer datos a través de la interfaz web.

##  Notificaciones

Sistema de notificaciones por email con templates HTML:

- **Verificación de Email**: Al registrarse
- **Reset de Password**: Recuperación de contraseña
- **Registro de Cuenta Game**: Verificación para crear cuenta de juego

##  Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.4.4**
- **Spring Security** - Autenticación y autorización
- **JWT (jjwt 0.12.6)** - Tokens de autenticación
- **Hibernate/JPA** - ORM para base de datos
- **MariaDB** - Base de datos
- **Firebase Admin SDK** - Autenticación con Google
- **Mercado Pago SDK** - Procesamiento de pagos
- **JavaMail** - Envío de emails
- **Logback** - Sistema de logging
- **Maven** - Gestión de dependencias

##  Dependencias Principales

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`
- `spring-boot-starter-validation`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `mariadb-java-client`
- `firebase-admin`
- `mercadopago-sdk`
- `bucket4j-core`

##  Configuración

### Base de Datos
Configuración de MariaDB en `application.properties`:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/l2jmobiusclassic
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
```

### Email
Configuración de JavaMail en `application.properties`:
```properties
spring.mail.host=smtp.tu-servidor.com
spring.mail.port=465
spring.mail.username=tu_email@ejemplo.com
```

### Mercado Pago
```properties
mercadopago.access.token=APP_USR-...
mercadopago.public.key=APP_USR-...
mercadopago.sandbox.enabled=true
```

### Firebase
- Credenciales en `service-account-key.json`

## 🚀 Instalacion

### Requisitos Previos
- Java 17 o superior
- Maven 3.6+
- MariaDB 10.5+
- Git

### Pasos de Instalacion

1. **Clonar el repositorio**
```bash
git clone https://github.com/Ak4n1/api-terra.git
cd api-terra/terra-api
```

2. **Configurar la base de datos**
   - Crear una base de datos MariaDB para el juego
   - Importar el schema si es necesario

3. **Configurar application.properties**
   - Copiar `src/main/resources/application.properties.example` a `application.properties`
   - Configurar las siguientes propiedades:
     - URL de base de datos MariaDB
     - Usuario y password de BD
     - Configuracion de email (SMTP)
     - Tokens de Mercado Pago
     - JWT secret key
     - Firebase credentials

4. **Configurar Firebase**
   - Descargar `service-account-key.json` desde Firebase Console
   - Colocarlo en `src/main/resources/`

5. **Compilar y ejecutar**
```bash
# Con Maven Wrapper (sin necesidad de tener Maven instalado)
./mvnw clean package
./mvnw spring-boot:run

# O con Maven instalado
mvn clean package
mvn spring-boot:run
```

6. **Verificar que funciona**
```bash
curl http://localhost:8080/api/health
```

La API estara disponible en `http://localhost:8080`

## 🚀 Ejecucion

```bash
cd terra-api
./mvnw spring-boot:run
```

O con Maven instalado:

```bash
cd terra-api
mvn spring-boot:run
```

## 📝 Endpoints Principales

### Auth
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Login con email/password
- `POST /api/auth/google/login` - Login con Google
- `GET /api/auth/me` - Información del usuario actual
- `POST /api/auth/resend-verification` - Reenviar email de verificación
- `GET /api/auth/recent-activity` - Actividad reciente del usuario

### Game
- `GET /api/game/characters/by-email` - Personajes por email
- `GET /api/game/clans/{clanId}` - Información de clan
- `GET /api/game/ranking/top-pvp` - Ranking PvP
- `GET /api/game/ranking/top-pk` - Ranking PK
- `GET /api/game/offline-market` - Mercado offline

### Payments
- `POST /api/payments/create-preference` - Crear preferencia de pago
- `POST /api/payments/webhook` - Webhook de Mercado Pago

## 📄 Licencia

Desarrollado para L2 Terra Online

