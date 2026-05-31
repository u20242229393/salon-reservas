package co.edu.usco.reservas.repository;

import co.edu.usco.reservas.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ServicioRepository — Repositorio JPA para la tabla servicios.
 *
 * Provee acceso a los servicios del catálogo del salón.
 * Incluye un método específico para mostrar solo servicios activos
 * en el formulario de reservas (no se muestran los desactivados).
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Devuelve solo los servicios activos (activo = true).
     * Usado en el formulario de reservas para que las clientas
     * solo vean servicios disponibles actualmente.
     * Los servicios desactivados no se eliminan (soft delete).
     */
    List<Servicio> findByActivoTrue();

    /**
     * Filtra servicios por estado activo/inactivo.
     * Usado en el panel de administración para listar
     * todos los servicios o solo los activos/inactivos.
     */
    List<Servicio> findByActivo(boolean activo);
}
