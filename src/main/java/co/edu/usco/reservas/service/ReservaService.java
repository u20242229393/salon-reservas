package co.edu.usco.reservas.service;

import co.edu.usco.reservas.entity.Reserva;
import co.edu.usco.reservas.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ReservaService — Lógica de negocio para la gestión de citas.
 *
 * Actúa como intermediario entre los controladores y el repositorio.
 * Los controladores nunca acceden directamente al repositorio — siempre
 * pasan por el service para mantener la lógica de negocio centralizada.
 *
 * Responsabilidades:
 * - Guardar nuevas reservas con estado inicial PENDIENTE
 * - Listar historial por cliente o profesional, con filtro por estado
 * - Cambiar el estado de una reserva (REALIZADO, CANCELADO)
 *
 * @Service registra la clase como Bean de servicio en el contexto de Spring.
 */
@Service
public class ReservaService {

    /** Repositorio JPA para operaciones CRUD en la tabla reservas */
    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Guarda una nueva reserva o actualiza una existente.
     * Si la reserva no tiene ID (es nueva), fuerza el estado PENDIENTE.
     *
     * @param reserva Objeto Reserva con todos los datos del formulario
     * @return La reserva guardada con el ID asignado por PostgreSQL
     */
    public Reserva guardarReserva(Reserva reserva) {
        if (reserva.getId() == null) {
            reserva.setEstado("PENDIENTE");
        }
        return reservaRepository.save(reserva);
    }

    /** Obtiene todas las reservas del sistema (para el dashboard de admin) */
    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    /** Busca una reserva por su ID (para la API REST) */
    public Optional<Reserva> obtenerPorId(Long id) {
        return reservaRepository.findById(id);
    }

    /**
     * Lista el historial de citas de una especialista.
     *
     * @param profesionalId ID de la especialista
     * @param estado        Filtro por estado: "PENDIENTE", "REALIZADO", "CANCELADO"
     *                      Si es null, devuelve todas sin filtrar
     */
    public List<Reserva> listarHistorialProfesional(Long profesionalId, String estado) {
        if (estado == null) return reservaRepository.buscarPorProfesionalId(profesionalId);
        return reservaRepository.findByProfesionalIdAndEstado(profesionalId, estado);
    }

    /**
     * Lista el historial de citas de una clienta.
     * Usado en el perfil y dashboard de la clienta.
     *
     * @param clienteId ID de la clienta
     * @param estado    Filtro por estado o null para todas
     */
    public List<Reserva> listarHistorialCliente(Long clienteId, String estado) {
        if (estado == null) return reservaRepository.findByClienteId(clienteId);
        return reservaRepository.findByClienteIdAndEstado(clienteId, estado);
    }

    /**
     * Obtiene todas las citas de una especialista (agenda completa).
     * Usado en la vista de agenda de la especialista.
     */
    public List<Reserva> obtenerAgendaPorProfesional(Long profesionalId) {
        return reservaRepository.buscarPorProfesionalId(profesionalId);
    }

    /**
     * Cambia el estado de una reserva existente.
     * Usado por la administradora para marcar citas como realizadas o canceladas.
     *
     * @param id          ID de la reserva a modificar
     * @param nuevoEstado Nuevo estado: "REALIZADO" o "CANCELADO"
     * @return true si la reserva fue encontrada y actualizada, false si no existe
     */
    public boolean cambiarEstadoReserva(Long id, String nuevoEstado) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setEstado(nuevoEstado);
            reservaRepository.save(reserva);
            return true;
        }
        return false;
    }
}
