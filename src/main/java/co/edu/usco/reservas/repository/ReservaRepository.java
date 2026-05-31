package co.edu.usco.reservas.repository;

import co.edu.usco.reservas.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReservaRepository — Repositorio JPA para operaciones con la tabla reservas.
 *
 * Extiende JpaRepository que provee automáticamente los métodos CRUD básicos:
 * save(), findById(), findAll(), deleteById(), etc.
 *
 * Los métodos adicionales se declaran aquí siguiendo la convención de nombres
 * de Spring Data JPA (Query Methods):
 * Spring genera automáticamente el SQL a partir del nombre del método.
 * Ejemplo: findByClienteId(Long id) → SELECT * FROM reservas WHERE cliente_id = ?
 *
 * @Repository indica que es un componente de acceso a datos.
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Busca las citas de una especialista filtradas por estado.
     * Usado en la agenda de especialista para separar pendientes de realizadas.
     *
     * @param profesionalId ID de la especialista
     * @param estado        "PENDIENTE", "REALIZADO" o "CANCELADO"
     */
    List<Reserva> findByProfesionalIdAndEstado(Long profesionalId, String estado);

    /**
     * Busca las citas de una clienta filtradas por estado.
     * Usado en el perfil para mostrar pendientes y realizadas por separado.
     *
     * @param clienteId ID de la clienta
     * @param estado    "PENDIENTE", "REALIZADO" o "CANCELADO"
     */
    List<Reserva> findByClienteIdAndEstado(Long clienteId, String estado);

    /**
     * Busca todas las citas de una clienta sin filtro de estado.
     * Usado para el historial completo en el perfil y dashboard.
     */
    List<Reserva> findByClienteId(Long clienteId);

    /**
     * Busca todas las citas de una especialista ordenadas por fecha.
     * Usa JPQL (@Query) para poder especificar el ORDER BY.
     * Spring Data JPA no puede inferir el ordenamiento solo del nombre del método
     * en combinación con un @Param.
     *
     * JPQL usa nombres de campos de la entidad Java (profesional.id),
     * no nombres de columnas de la BD (profesional_id).
     */
    @Query("SELECT r FROM Reserva r WHERE r.profesional.id = :profesionalId ORDER BY r.fechaHoraCita ASC")
    List<Reserva> buscarPorProfesionalId(@Param("profesionalId") Long profesionalId);
}
