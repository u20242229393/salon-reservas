package co.edu.usco.reservas.entity;

import jakarta.persistence.*;

/**
 * Usuario — Entidad principal de usuarios del sistema.
 *
 * Representa a TODOS los usuarios independientemente de su rol.
 * El campo "rol" determina qué puede hacer cada usuario:
 *
 *   ROLE_ADMIN       → Administradora del salón (acceso total)
 *   ROLE_PROFESIONAL → Especialista / manicurista (ve su agenda)
 *   ROLE_CLIENTE     → Clienta que agenda citas
 *
 * Diseño de un solo tipo de usuario (tabla única):
 * Se eligió este enfoque en lugar de herencia de tablas para simplificar
 * las consultas y el manejo de Spring Security. El rol diferencia el comportamiento.
 *
 * Soft delete: el campo "activo" permite desactivar usuarios sin eliminarlos,
 * preservando el historial de citas asociadas.
 *
 * Extiende AuditoriaBase para tener fecha_creacion y fecha_actualizacion.
 */
@Entity
@Table(name = "usuarios")
public class Usuario extends AuditoriaBase {

    /** ID generado automáticamente por la secuencia de PostgreSQL */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del usuario (máximo 100 caracteres) */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Correo electrónico — sirve como nombre de usuario para el login.
     * unique = true garantiza que no haya dos usuarios con el mismo correo.
     * Spring Security usa este campo como "username" en UserDetailsService.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String correo;

    /**
     * Contraseña almacenada como hash BCrypt.
     * NUNCA se almacena la contraseña en texto plano.
     * BCryptPasswordEncoder genera hashes del tipo: $2a$10$...
     *
     * Para usuarios de Google OAuth2, este campo tiene el valor
     * "GOOGLE_OAUTH2_NO_PASSWORD" (no pueden hacer login con contraseña).
     */
    @Column(nullable = false, length = 255)
    private String password;

    /** Número de teléfono (opcional, para contacto por WhatsApp) */
    @Column(length = 20)
    private String telefono;

    /**
     * Rol del usuario en el sistema.
     * Valores posibles: ROLE_ADMIN, ROLE_PROFESIONAL, ROLE_CLIENTE
     * Spring Security usa este campo para controlar el acceso por URL.
     */
    @Column(nullable = false, length = 20)
    private String rol;

    /**
     * Estado activo/inactivo del usuario (soft delete).
     * false = desactivado (no puede iniciar sesión, no aparece en listados)
     * true  = activo (funcionamiento normal)
     * Por defecto todos los usuarios se crean activos.
     */
    @Column(nullable = false)
    private boolean activo = true;

    // Getters y setters estándar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
