package co.edu.usco.reservas.repository;

import co.edu.usco.reservas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UsuarioRepository — Repositorio JPA para la tabla usuarios.
 *
 * Gestiona todos los usuarios del sistema sin importar el rol.
 * Spring Data JPA genera automáticamente el SQL a partir del nombre
 * de los métodos (Query Methods).
 *
 * Métodos heredados de JpaRepository (automáticos):
 * - save(usuario): INSERT o UPDATE
 * - findById(id): SELECT WHERE id = ?
 * - findAll(): SELECT * FROM usuarios
 * - deleteById(id): DELETE WHERE id = ?
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     * Retorna Optional para manejar el caso de usuario no encontrado
     * sin lanzar excepción directamente.
     *
     * Usos principales:
     * - UsuarioDetailsService: cargar usuario al hacer login
     * - GoogleOAuth2SuccessHandler: verificar si el usuario de Google existe
     * - UsuarioService.existePorCorreo(): validar unicidad en registro
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Lista todos los usuarios de un rol específico.
     * Usos:
     * - findByRol("ROLE_PROFESIONAL"): cargar especialistas en el formulario de reservas
     * - findByRol("ROLE_CLIENTE"): listar clientas en la API REST
     * - findByRol("ROLE_PROFESIONAL"): gestión de especialistas en el admin
     */
    List<Usuario> findByRol(String rol);
}
