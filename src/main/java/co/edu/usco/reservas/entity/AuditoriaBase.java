package co.edu.usco.reservas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditoriaBase — Clase base de auditoría para todas las entidades.
 *
 * Propósito:
 * Centraliza los campos de auditoría (fechaCreacion, fechaActualizacion)
 * que deben estar en TODAS las entidades del sistema (Usuario, Reserva, etc.).
 * En lugar de repetir estos campos en cada entidad, se heredan de aquí.
 *
 * @MappedSuperclass indica a JPA que esta clase NO tiene tabla propia en la BD.
 * Sus campos se incluyen en la tabla de cada clase que la extienda.
 *
 * Ejemplo de herencia:
 *   public class Usuario extends AuditoriaBase → tabla usuarios tendrá
 *   fecha_creacion y fecha_actualizacion automáticamente.
 *
 * Patrón utilizado: Template Method + JPA Lifecycle Callbacks
 */
@MappedSuperclass
public abstract class AuditoriaBase {

    /**
     * Fecha y hora en que se creó el registro.
     * - nullable = false: obligatorio, nunca puede ser null
     * - updatable = false: una vez creado, no se modifica
     * - columnDefinition: valor por defecto en la BD (CURRENT_TIMESTAMP)
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro.
     * Se actualiza automáticamente cada vez que se guarda el objeto.
     */
    @Column(name = "fecha_actualizacion",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;

    /**
     * Callback de JPA — se ejecuta ANTES de hacer INSERT en la BD.
     * Asigna las fechas de creación y actualización automáticamente.
     *
     * Sin este método, Hibernate intentaría insertar null en fecha_creacion
     * y fallaría por la restricción NOT NULL de la columna.
     *
     * IMPORTANTE: debe ser public (no protected) para que Hibernate
     * lo pueda invocar correctamente desde subclases.
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = ahora;
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = ahora;
        }
    }

    /**
     * Callback de JPA — se ejecuta ANTES de hacer UPDATE en la BD.
     * Actualiza la fecha de modificación cada vez que se guarda el objeto.
     */
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y setters estándar
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime f) { this.fechaActualizacion = f; }
}
