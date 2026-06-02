package co.edu.usco.reservas.config;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GoogleOAuth2SuccessHandler — Maneja el éxito del login con Google.
 *
 * Flujo completo del login con Google:
 * 1. La clienta hace clic en "Continuar con Google" en el login
 * 2. Spring Security redirige a Google para autenticación
 * 3. Google autentica y devuelve el código de autorización
 * 4. Spring Security intercambia el código por un token OAuth2
 * 5. Este handler recibe el control con los datos del usuario de Google
 * 6. Busca si el correo ya existe en la BD:
 *    - SÍ existe → usa ese usuario con su rol actual
 *    - NO existe → crea automáticamente un nuevo ROLE_CLIENTE
 * 7. Reemplaza la autenticación OAuth2 por una basada en roles de BD
 * 8. Redirige al dashboard correspondiente según el rol
 *
 * Por qué UsuarioRepository directamente (no UsuarioService):
 * UsuarioService inyecta SecurityConfig → SecurityConfig inyecta este handler
 * → esto crearía un ciclo de dependencias que Spring no puede resolver.
 * Usar el repository directamente rompe el ciclo.
 *
 * @Component registra esta clase como Bean de Spring (se inyecta automáticamente).
 */
@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Acceso directo a la BD para buscar y crear usuarios.
     * Se evita UsuarioService para no crear ciclo de dependencias con SecurityConfig.
     */
    private final UsuarioRepository usuarioRepository;

    public GoogleOAuth2SuccessHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Se ejecuta automáticamente después de que Google autentica exitosamente.
     *
     * @param request        Petición HTTP con datos de la sesión
     * @param response       Respuesta HTTP para hacer el redirect
     * @param authentication Objeto de autenticación OAuth2 con datos de Google
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        // Obtener los atributos del perfil de Google
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String correo = oauth2User.getAttribute("email");  // correo de Google
        String nombre = oauth2User.getAttribute("name");   // nombre completo de Google

        // Buscar si el usuario ya existe en la BD por su correo
        Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);
        Usuario usuario;

        if (existente.isPresent()) {
            // Usuario existente: entra con su rol y datos actuales
            usuario = existente.get();
        } else {
            // Usuario nuevo: crear automáticamente como ROLE_CLIENTE
            usuario = new Usuario();
            usuario.setCorreo(correo);
            usuario.setNombre(nombre != null ? nombre : correo);
            // No tiene contraseña — solo puede autenticarse con Google
            usuario.setPassword("GOOGLE_OAUTH2_NO_PASSWORD");
            usuario.setRol("ROLE_CLIENTE");
            usuario.setActivo(true);
            // Asignar fechas manualmente porque @PrePersist no se dispara aquí
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioRepository.save(usuario);
        }

        // Reemplazar la autenticación OAuth2 (sin roles de BD) por una
        // UsernamePasswordAuthenticationToken con el rol correcto de la BD.
        // Sin esto, Spring Security no sabría qué páginas puede ver el usuario.
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        usuario.getCorreo(),                              // principal (nombre de usuario)
                        null,                                             // credenciales (no necesarias)
                        List.of(new SimpleGrantedAuthority(usuario.getRol())) // autoridades (rol de BD)
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Redirigir al dashboard según el rol
        String redirect = switch (usuario.getRol()) {
            case "ROLE_ADMIN"       -> "/admin/dashboard";
            case "ROLE_PROFESIONAL" -> "/especialista/agenda";
            default                 -> "/cliente/dashboard";
        };
        response.sendRedirect(redirect);
    }
}
