package co.edu.usco.reservas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reserva — Entidad central del sistema de citas.
 *
 * Representa una cita agendada en el salón.
 * Conecta tres entidades: cliente (quién), profesional (con quién),
 * servicio (qué se va a hacer).
 *
 * Estados posibles de una reserva:
 *   PENDIENTE  → Cita agendada, aún no realizada
 *   REALIZADO  → Cita completada exitosamente
 *   CANCELADO  → Cita cancelada (no se elimina, soft cancel)
 *
 * Métodos de pago:
 *   EFECTIVO, TARJETA, TRANSFERENCIA, DATAFONO
 *
 * FetchType.EAGER en todas las relaciones:
 * Esto garantiza que al cargar una reserva, Hibernate también cargue
 * automáticamente el cliente, profesional y servicio asociados.
 * Sin EAGER, los templates de Thymeleaf mostrarían "r.cliente.nombre"
 * en texto literal en lugar del nombre real (LazyInitializationException).
 *
 * Extiende AuditoriaBase para tener fecha_creacion y fecha_actualizacion.
 */
@Entity
@Table(name = "reservas")
public class Reserva extends AuditoriaBase {

    /** ID generado automáticamente por la secuencia de PostgreSQL */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clienta que hizo la reserva.
     * ManyToOne: muchas reservas pueden pertenecer a un mismo cliente.
     * EAGER: siempre se carga junto con la reserva.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    /**
     * Especialista asignada a la cita.
     * ManyToOne: una especialista puede tener muchas reservas.
     * EAGER: necesario para mostrar el nombre en las alertas del dashboard.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Usuario profesional;

    /**
     * Servicio que se realizará en la cita.
     * ManyToOne: un servicio puede estar en muchas reservas.
     * EAGER: necesario para mostrar el nombre del servicio en los listados.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    /** Fecha y hora exacta de la cita */
    @Column(name = "fecha_hora_cita", nullable = false)
    private LocalDateTime fechaHoraCita;

    /** Estado actual: PENDIENTE, REALIZADO o CANCELADO */
    @Column(nullable = false, length = 20)
    private String estado;

    /** Método de pago elegido: EFECTIVO, TARJETA, TRANSFERENCIA, DATAFONO */
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    // Getters y setters estándar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }
    public Usuario getProfesional() { return profesional; }
    public void setProfesional(Usuario profesional) { this.profesional = profesional; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public LocalDateTime getFechaHoraCita() { return fechaHoraCita; }
    public void setFechaHoraCita(LocalDateTime f) { this.fechaHoraCita = f; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String m) { this.metodoPago = m; }
}
