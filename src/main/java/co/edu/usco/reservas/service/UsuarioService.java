package co.edu.usco.reservas.service;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UsuarioService — Lógica de negocio para la gestión de usuarios.
 *
 * Maneja las operaciones relacionadas con usuarios: registro, búsqueda
 * y actualización de perfil.
 *
 * Encriptación de contraseñas:
 * NUNCA se almacena una contraseña en texto plano. Antes de guardar
 * cualquier contraseña, se encripta con BCryptPasswordEncoder.
 * Este servicio recibe el PasswordEncoder de SecurityConfig mediante
 * inyección de dependencias.
 *
 * IMPORTANTE: Este service NO implementa UserDetailsService.
 * Spring Security usa UsuarioDetailsService (en el paquete config)
 * para evitar el ciclo de dependencias con SecurityConfig.
 *
 * @Service registra la clase como Bean de servicio de Spring.
 */
@Service
public class UsuarioService {

    /** Repositorio JPA para operaciones CRUD en la tabla usuarios */
    private final UsuarioRepository usuarioRepository;

    /**
     * Encriptador BCrypt — definido como Bean en SecurityConfig.
     * Spring lo inyecta automáticamente aquí.
     */
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Encripta la contraseña antes de guardar en la BD.
     * El rol debe estar definido antes de llamar a este método.
     *
     * @param usuario Usuario con todos los datos incluyendo contraseña en texto plano
     * @return El usuario guardado con contraseña hasheada e ID asignado
     */
    public Usuario registrarUsuario(Usuario usuario) {
        // Encriptar contraseña antes de guardar — NUNCA texto plano en BD
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Verifica si ya existe un usuario con el correo dado.
     * Usado en el registro para evitar duplicados.
     *
     * @param correo Correo a verificar
     * @return true si ya existe, false si está disponible
     */
    public boolean existePorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).isPresent();
    }

    /**
     * Busca un usuario por su correo electrónico.
     * Lanza excepción si no existe (para detectar errores de datos).
     *
     * @param correo Correo del usuario a buscar
     * @return El usuario encontrado
     * @throws UsernameNotFoundException Si el correo no existe en la BD
     */
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + correo));
    }

    /**
     * Actualiza los datos del perfil de un usuario.
     * Solo actualiza nombre, teléfono y opcionalmente la contraseña.
     * El correo NO se puede cambiar (es el identificador único).
     *
     * @param correoActual  Correo del usuario que está editando su perfil
     * @param datosNuevos   Objeto con el nuevo nombre y teléfono
     * @param passwordNuevo Nueva contraseña en texto plano, o null/blank para no cambiar
     */
    public void actualizarPerfil(String correoActual, Usuario datosNuevos, String passwordNuevo) {
        // Buscar el usuario actual en la BD
        Usuario usuario = buscarPorCorreo(correoActual);

        // Actualizar solo los campos editables
        usuario.setNombre(datosNuevos.getNombre());
        usuario.setTelefono(datosNuevos.getTelefono());

        // Actualizar contraseña solo si se proporcionó una nueva
        if (passwordNuevo != null && !passwordNuevo.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        }

        usuarioRepository.save(usuario);
    }
}
