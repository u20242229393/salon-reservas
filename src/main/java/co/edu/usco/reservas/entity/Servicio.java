package co.edu.usco.reservas.entity;

import jakarta.persistence.*;

/**
 * Servicio — Entidad del catálogo de servicios del salón.
 *
 * Representa cada servicio que ofrece The Girls Club
 * (Acrílicas Esculpidas, Diseño Semipermanente, etc.).
 *
 * Soft delete con campo "activo":
 * Los servicios NO se eliminan de la base de datos porque existen
 * reservas históricas que los referencian. Si se eliminara un servicio,
 * las reservas pasadas perderían su referencia (FK rota).
 * En cambio, se desactivan: activo=false los oculta para nuevas reservas
 * pero preserva el historial.
 *
 * Campo imagenUrl:
 * Almacena la URL de una imagen de referencia para mostrar en el catálogo.
 * Puede ser una URL externa (https://...) o una ruta local (/images/servicios/...).
 * Las imágenes locales deben colocarse en:
 * src/main/resources/static/images/servicios/
 */
@Entity
@Table(name = "servicios")
public class Servicio {

    /** ID generado automáticamente por la secuencia de PostgreSQL */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del servicio que verán las clientas al reservar */
    @Column(nullable = false)
    private String nombre;

    /** Descripción detallada del servicio (opcional) */
    private String descripcion;

    /** Duración estimada en minutos — ayuda a organizar la agenda */
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    /** Precio en pesos colombianos (COP) */
    @Column(nullable = false)
    private Double precio;

    /**
     * Indica si el servicio está disponible para nuevas reservas.
     * true  = activo (aparece en el formulario de reservas)
     * false = inactivo (oculto para nuevas reservas, historial preservado)
     * Por defecto todos los servicios se crean activos.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * URL de la imagen de referencia del servicio.
     * Se muestra en el catálogo de servicios para que las clientas
     * puedan ver cómo quedan las uñas antes de agendar.
     * Máximo 500 caracteres para soportar URLs largas.
     */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    // Constructor vacío requerido por JPA
    public Servicio() {}

    /**
     * Constructor de conveniencia para crear servicios desde código.
     * No incluye imagenUrl porque es opcional.
     */
    public Servicio(Long id, String nombre, String descripcion,
                    Integer duracionMinutos, Double precio, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.precio = precio;
        this.activo = activo;
    }

    // Getters y setters estándar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer d) { this.duracionMinutos = d; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
