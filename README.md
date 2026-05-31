# 💅 The Girls Club — Sistema de Reservas

> Sistema web de reservas para salón de uñas desarrollado con Spring Boot 4, Java 21, Thymeleaf y PostgreSQL.
> Proyecto final — Programación Web · Universidad Surcolombiana (USCO) · 2026

---

## 📋 Tabla de contenidos

1. [Descripción del proyecto](#descripción-del-proyecto)
2. [Tecnologías utilizadas](#tecnologías-utilizadas)
3. [Arquitectura del proyecto](#arquitectura-del-proyecto)
4. [Requisitos previos](#requisitos-previos)
5. [Configuración e instalación](#configuración-e-instalación)
6. [Credenciales de prueba](#credenciales-de-prueba)
7. [Funcionalidades por rol](#funcionalidades-por-rol)
8. [API REST y Swagger](#api-rest-y-swagger)
9. [Internacionalización i18n](#internacionalización-i18n)
10. [Accesibilidad WCAG AA](#accesibilidad-wcag-aa)
11. [HTTPS y certificado SSL](#https-y-certificado-ssl)

---

## Descripción del proyecto

The Girls Club es un sistema completo de reservas en línea para un salón de uñas premium ubicado en Neiva, Huila, Colombia. Permite a las clientas agendar citas, a las especialistas ver su agenda y a la administradora gestionar todo el negocio.

### Características principales

- Autenticación con correo/contraseña y **Google OAuth2**
- **3 roles de usuario**: Administradora, Especialista, Clienta
- Calendario visual interactivo para ver y agendar citas
- Gestión de servicios con imagen de referencia (catálogo)
- Gestión de especialistas con activar/desactivar
- Agenda por día para la administradora
- **4 idiomas**: Español, English, Português, Italiano
- Accesibilidad **WCAG 2.1 nivel AA**
- Comunicación segura con **HTTPS (TLS/SSL)**
- API REST documentada con **Swagger / OpenAPI 3**

---

## Tecnologías utilizadas

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Backend | Spring Boot | 4.0.6 |
| Lenguaje | Java | 21 |
| Plantillas | Thymeleaf | 3.x |
| Seguridad | Spring Security + OAuth2 | 7.x |
| Base de datos | PostgreSQL | 16.x |
| ORM | Hibernate / JPA | 7.x |
| Frontend | Bootstrap | 5.3.2 |
| Documentación API | SpringDoc OpenAPI | 2.8.6 |
| Build | Maven | 3.x |
| IDE | IntelliJ IDEA | - |

---

## Arquitectura del proyecto

```
src/main/java/co/edu/usco/reservas/
├── config/
│   ├── SecurityConfig.java          # Spring Security, roles, OAuth2, sesiones
│   ├── GoogleOAuth2SuccessHandler.java  # Manejo del login con Google
│   ├── HttpsRedirectConfig.java     # Redirect automático HTTP → HTTPS
│   ├── I18nConfig.java              # Internacionalización (SessionLocaleResolver)
│   ├── SwaggerConfig.java           # Configuración OpenAPI
│   └── UsuarioDetailsService.java   # UserDetailsService para autenticación
├── controller/
│   ├── AdminController.java         # Dashboard, agenda, especialistas, servicios
│   ├── AuthController.java          # Login, registro, perfil, dashboard clienta
│   ├── EspecialistaController.java  # Agenda especialista, marcar atendida
│   ├── HomeController.java          # Página de inicio, contacto, error 403
│   └── ReservaController.java       # Formulario de reservas
├── api/
│   ├── ReservaApiController.java    # REST API de reservas (Swagger)
│   └── UsuarioApiController.java    # REST API de usuarios (Swagger)
├── entity/
│   ├── AuditoriaBase.java           # fechaCreacion y fechaActualizacion (herencia)
│   ├── Reserva.java                 # Entidad de citas (EAGER fetch)
│   ├── Servicio.java                # Entidad de servicios del catálogo
│   └── Usuario.java                 # Entidad de usuarios (todos los roles)
├── repository/
│   ├── ReservaRepository.java
│   ├── ServicioRepository.java
│   └── UsuarioRepository.java
└── service/
    ├── ReservaService.java
    └── UsuarioService.java

src/main/resources/
├── templates/
│   ├── layout/layout.html           # Plantilla base (navbar, footer, idiomas)
│   ├── login.html                   # Login con Google OAuth2 + selector de idioma
│   ├── registro/formulario.html     # Registro de nuevas clientas
│   ├── perfil.html                  # Perfil + historial de citas
│   ├── cliente/dashboard.html       # Calendario interactivo de la clienta
│   ├── reservas/formulario.html     # Formulario de nueva cita
│   ├── admin/
│   │   ├── dashboard.html           # Panel de control administradora
│   │   ├── agenda.html              # Agenda por día (navegable)
│   │   ├── especialistas.html       # Gestión del equipo
│   │   └── servicios.html           # Catálogo de servicios
│   ├── especialista/agenda.html     # Agenda de la especialista
│   └── error/403.html               # Página de acceso denegado
├── i18n/
│   ├── messages.properties          # Español (por defecto)
│   ├── messages_en.properties       # Inglés
│   ├── messages_pt.properties       # Portugués
│   └── messages_it.properties       # Italiano
├── database/
│   ├── schema.sql                   # Creación de tablas
│   └── data.sql                     # Datos iniciales de prueba
├── static/
│   └── images/servicios/            # Imágenes de referencia de los servicios
├── application.properties           # Configuración general
└── thegirlsclub.p12                 # Certificado SSL (PKCS12)
```

---

## Requisitos previos

- **Java 21** o superior
- **Maven 3.x**
- **PostgreSQL 16** corriendo en `localhost:5432`
- **IntelliJ IDEA** (recomendado)
- Base de datos creada: `salon_reservas`

---

## Configuración e instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/u20242229393/salon-reservas.git
cd salon-reservas
```

### 2. Crear la base de datos en PostgreSQL

```sql
CREATE DATABASE salon_reservas;
```

### 3. Configurar `application.properties`

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/salon_reservas
spring.datasource.username=postgres
spring.datasource.password=TU_PASSWORD

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET
```

### 4. Ejecutar el proyecto

```bash
mvn spring-boot:run
```

### 5. Acceder al sistema

- **HTTP**: `http://localhost:8080`
- **HTTPS**: `https://localhost:8443` (requiere `.p12` en `resources/`)

### 6. Limpiar y reiniciar la base de datos

Si necesitas reiniciar los datos desde cero, ejecuta en pgAdmin:

```sql
-- Eliminar tablas
DROP TABLE IF EXISTS reservas CASCADE;
DROP TABLE IF EXISTS servicios CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- Reiniciar el proyecto — las tablas se recrean desde schema.sql y data.sql automáticamente
```

Si solo necesitas corregir la secuencia de IDs (error de llave duplicada):

```sql
SELECT setval('servicios_id_seq', (SELECT MAX(id) FROM servicios));
SELECT setval('usuarios_id_seq',  (SELECT MAX(id) FROM usuarios));
SELECT setval('reservas_id_seq',  (SELECT MAX(id) FROM reservas));
```

---

## Credenciales de prueba

| Rol | Correo | Contraseña |
|-----|--------|-----------|
| Administradora | admin@thegirlsclub.com | 123456 |
| Especialista | lina.cardozo@thegirlsclub.com | 123456 |
| Especialista | diana.silva@thegirlsclub.com | 123456 |
| Clienta | valeria@gmail.com | 123456 |

---

## Funcionalidades por rol

### 👑 Administradora
- Dashboard con estadísticas, calendario y citas del día
- Recordatorios automáticos de cobro presencial (efectivo, datáfono, transferencia)
- Agenda navegable por día (cualquier fecha pasada o futura)
- Gestión de especialistas: crear, activar/desactivar
- Gestión de servicios: crear con imagen, precio y duración; activar/desactivar (soft delete)
- Ver y gestionar todas las reservas

### 💅 Especialista
- Ver agenda personal con citas pendientes y realizadas
- Marcar citas como atendidas

### 🌸 Clienta
- Dashboard con calendario visual interactivo
- Clic en día disponible → abre formulario de reserva con fecha prellenada
- Clic en día con cita → muestra detalle
- Historial de citas pendientes y realizadas
- Editar perfil personal

---

## API REST y Swagger

La documentación interactiva está disponible en:

```
https://localhost:8443/swagger-ui/index.html
```

### Endpoints disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reservas` | Listar todas las reservas |
| GET | `/api/reservas/{id}` | Obtener reserva por ID |
| GET | `/api/reservas/cliente/{id}` | Reservas de una clienta |
| GET | `/api/reservas/especialista/{id}` | Agenda de una especialista |
| PATCH | `/api/reservas/{id}/cancelar` | Cancelar reserva |
| PATCH | `/api/reservas/{id}/realizado` | Marcar como realizada |
| GET | `/api/usuarios/especialistas` | Listar especialistas |
| GET | `/api/usuarios/clientas` | Listar clientas |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |

---

## Internacionalización i18n

El sistema soporta 4 idiomas configurados con `SessionLocaleResolver` (el idioma se guarda en la sesión HTTP).

| Idioma | Código | Archivo |
|--------|--------|---------|
| Español | `es` | `messages.properties` |
| Inglés | `en` | `messages_en.properties` |
| Portugués | `pt` | `messages_pt.properties` |
| Italiano | `it` | `messages_it.properties` |

Para cambiar el idioma: agregar `?lang=en` (o `pt`, `it`, `es`) a cualquier URL.

---

## Accesibilidad WCAG AA

El sistema cumple los criterios **WCAG 2.1 nivel AA** verificados con TAW:

- ✅ **1.1.1** Imágenes decorativas con `aria-hidden="true"`
- ✅ **1.3.1** Estructura semántica: `<main>`, `<nav>`, `<aside>`, `<section>`, `<article>`
- ✅ **1.4.3** Contraste de color mínimo 4.5:1 en todos los textos
- ✅ **2.1.1** Navegación completa por teclado con `tabindex` y `onkeypress`
- ✅ **2.4.1** Skip link "Saltar al contenido principal"
- ✅ **2.4.7** Focus visible con `outline: 3px solid var(--red-wine)`
- ✅ **3.3.1** Mensajes de error con `role="alert"` y `aria-live="assertive"`
- ✅ **3.3.2** Labels asociados a inputs con `for/id`
- ✅ **4.1.3** Contenido dinámico con `aria-live="polite"`

---

## HTTPS y certificado SSL

El certificado fue generado con **Keystore Explorer** siguiendo las guías del docente:

- **Tipo**: PKCS12 (.p12)
- **Algoritmo**: RSA 2048 bits
- **Firma**: SHA256withRSA
- **Validez**: 3650 días (10 años)
- **SAN**: DNS `localhost` + IP `127.0.0.1`
- **Alias**: `thegirlsclub`
- **Contraseña**: `Usco1234`

Para activar HTTPS, descomentar en `application.properties`:

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:thegirlsclub.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=Usco1234
server.ssl.key-alias=thegirlsclub
```

---

## 👩‍💻 Desarrollado por

Valeria Vargas Artunduaga — 20242229393

Proyecto Final — Programación Web  
Universidad Surcolombiana — USCO  
Neiva, Huila, Colombia · 2026
