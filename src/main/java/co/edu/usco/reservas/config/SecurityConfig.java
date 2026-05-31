package co.edu.usco.reservas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SecurityConfig — Configuración central de seguridad del sistema.
 *
 * Define TODAS las reglas de acceso del sistema:
 * - Qué URLs son públicas y cuáles requieren autenticación
 * - Qué roles pueden acceder a qué secciones
 * - Cómo funciona el login (formulario + Google OAuth2)
 * - Cómo funciona el logout
 * - Encriptación de contraseñas con BCrypt
 * - Gestión de sesiones (migrateSession preserva el idioma i18n)
 *
 * Roles del sistema:
 *   ROLE_ADMIN       → /admin/**
 *   ROLE_PROFESIONAL → /especialista/**
 *   ROLE_CLIENTE     → /cliente/**, /reservas/**, /perfil/**
 *
 * @Configuration indica que esta clase contiene definiciones de Beans.
 * @EnableWebSecurity activa la seguridad web de Spring.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** UserDetailsService personalizado para cargar usuarios desde PostgreSQL */
    private final UsuarioDetailsService usuarioDetailsService;

    /** Handler para procesar el éxito del login con Google OAuth2 */
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
    }

    /**
     * Bean de encriptación de contraseñas.
     * BCrypt es el algoritmo recomendado: genera hashes con sal aleatoria
     * y es computacionalmente costoso (resistente a ataques de fuerza bruta).
     * Factor de coste por defecto: 10 iteraciones.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Handler de éxito para el login con formulario (correo + contraseña).
     * Redirige al dashboard correspondiente según el rol del usuario:
     * - ROLE_ADMIN       → /admin/dashboard
     * - ROLE_PROFESIONAL → /especialista/agenda
     * - ROLE_CLIENTE     → /cliente/dashboard
     */
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication auth) -> {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/dashboard");
            } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROFESIONAL"))) {
                response.sendRedirect("/especialista/agenda");
            } else {
                response.sendRedirect("/cliente/dashboard");
            }
        };
    }

    /**
     * Cadena de filtros de seguridad — configuración principal.
     *
     * Cada llamada encadenada configura un aspecto diferente:
     * - csrf: deshabilitado para simplificar formularios con Thymeleaf
     * - authorizeHttpRequests: reglas de acceso por URL y rol
     * - formLogin: login con formulario (correo + contraseña)
     * - oauth2Login: login con Google
     * - logout: cierre de sesión
     * - sessionManagement: migrateSession preserva el idioma i18n
     * - exceptionHandling: página de error 403 personalizada
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deshabilitado — en proyectos con Thymeleaf y Spring MVC
            // se puede deshabilitar si se controla bien el acceso por roles
            .csrf(csrf -> csrf.disable())
            .userDetailsService(usuarioDetailsService)

            // ── Reglas de acceso por URL ────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos: página principal, CSS, JS, imágenes
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()
                // Páginas públicas: catálogo de servicios, contacto, registro
                .requestMatchers("/servicios/**", "/contactanos/**", "/registro/**").permitAll()
                // Página de login (pública obviamente)
                .requestMatchers("/login", "/login/**").permitAll()
                // Swagger UI: documentación de la API (pública para presentación)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/api-docs/**").permitAll()
                // API REST: pública para pruebas con Postman
                .requestMatchers("/api/**").permitAll()
                // Páginas por rol — solo el rol correspondiente puede acceder
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/especialista/**").hasAuthority("ROLE_PROFESIONAL")
                .requestMatchers("/cliente/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                .requestMatchers("/reservas/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                .requestMatchers("/perfil/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_PROFESIONAL")
                // Páginas de error: accesibles sin autenticación
                .requestMatchers("/error/**").permitAll()
                // Cualquier otra URL requiere autenticación
                .anyRequest().authenticated()
            )

            // ── Login con formulario ────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                    // Página de login personalizada
                .usernameParameter("username")          // Nombre del campo de correo en el form
                .passwordParameter("password")          // Nombre del campo de contraseña
                .successHandler(successHandler())       // Redirige según el rol
                .failureUrl("/login?error=true")        // En caso de error de credenciales
                .permitAll()
            )

            // ── Login con Google OAuth2 ─────────────────────────────────
            // GoogleOAuth2SuccessHandler crea el usuario si no existe
            // y redirige según su rol igual que el login normal
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(googleOAuth2SuccessHandler)
                .failureUrl("/login?error=true")
            )

            // ── Logout ──────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")                           // URL que dispara el logout
                .logoutSuccessUrl("/login?logout=true")         // Redirige al login con mensaje
                .invalidateHttpSession(true)                    // Destruye la sesión HTTP
                .clearAuthentication(true)                      // Limpia el SecurityContext
                .deleteCookies("JSESSIONID")                    // Elimina la cookie de sesión
                .permitAll()
            )

            // ── Gestión de sesiones ─────────────────────────────────────
            // migrateSession: al autenticar, Spring Security migra la sesión
            // existente en lugar de crear una nueva.
            // Esto es CRÍTICO para que el idioma elegido en el login
            // (guardado por SessionLocaleResolver) persista en el dashboard.
            // Sin esto, el login crea una sesión nueva y pierde el idioma.
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )

            // ── Manejo de excepciones ───────────────────────────────────
            // Redirige a la página 403 personalizada cuando un usuario
            // intenta acceder a una URL para la que no tiene permiso
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
