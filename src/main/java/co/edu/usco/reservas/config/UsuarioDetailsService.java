package co.edu.usco.reservas.config;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UsuarioDetailsService — Puente entre la BD y Spring Security.
 *
 * Spring Security necesita cargar los datos del usuario al autenticar.
 * Esta clase implementa UserDetailsService para decirle a Spring Security
 * cómo buscar un usuario en NUESTRA base de datos PostgreSQL.
 *
 * Sin esta clase, Spring Security no sabría dónde están los usuarios
 * ni cómo verificar sus contraseñas.
 *
 * IMPORTANTE: Esta clase NO debe inyectar UsuarioService porque
 * UsuarioService inyecta SecurityConfig → ciclo de dependencias.
 * Por eso usa UsuarioRepository directamente.
 *
 * @Service registra la clase como Bean de servicio de Spring.
 * El nombre "usuarioDetailsService" es referenciado en SecurityConfig.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    /** Acceso directo a la tabla usuarios de PostgreSQL */
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga los datos del usuario por su nombre de usuario (correo electrónico).
     *
     * Spring Security llama a este método automáticamente al hacer login.
     * Recibe el correo que escribió el usuario y debe devolver un UserDetails
     * con la contraseña hasheada y los roles.
     *
     * Spring Security luego compara la contraseña ingresada con el hash
     * usando BCryptPasswordEncoder.matches().
     *
     * @param correo El correo electrónico ingresado en el formulario de login
     * @return UserDetails con correo, contraseña hasheada y rol
     * @throws UsernameNotFoundException Si el correo no existe en la BD
     */
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        // Buscar el usuario en la BD por correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con el correo: " + correo));

        // Construir el objeto UserDetails que Spring Security entiende
        // SimpleGrantedAuthority convierte el rol String en una autoridad de Spring Security
        return new User(
                usuario.getCorreo(),                                     // username
                usuario.getPassword(),                                   // contraseña hasheada
                List.of(new SimpleGrantedAuthority(usuario.getRol()))   // roles/autoridades
        );
    }
}
