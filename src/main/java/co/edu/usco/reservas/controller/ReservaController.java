package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.entity.Reserva;
import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.ReservaRepository;
import co.edu.usco.reservas.repository.ServicioRepository;
import co.edu.usco.reservas.repository.UsuarioRepository;
import co.edu.usco.reservas.service.ReservaService;
import co.edu.usco.reservas.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReservaController — Controlador del formulario de reservas.
 *
 * Gestiona el proceso de agendar una nueva cita:
 * 1. GET /reservas/formulario → muestra el formulario con servicios y especialistas
 * 2. POST /reservas/guardar  → guarda la reserva y redirige con mensaje de éxito
 *
 * Acceso: ROLE_CLIENTE y ROLE_ADMIN (definido en SecurityConfig).
 *
 * El formulario permite seleccionar:
 * - Servicio (solo los activos: findByActivoTrue)
 * - Especialista (profesionales activos)
 * - Fecha y hora disponible
 * - Método de pago (Efectivo, Nequi/PSE, Tarjeta, Datáfono)
 */
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    public ReservaController(ServicioRepository servicioRepository,
                              UsuarioRepository usuarioRepository,
                              ReservaRepository reservaRepository,
                              ReservaService reservaService,
                              UsuarioService usuarioService) {
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra el formulario para agendar una nueva cita.
     *
     * Carga los datos necesarios para los selectores del formulario:
     * - servicios: solo los activos (activo=true) para no mostrar servicios retirados
     * - profesionales: todas las especialistas con ROLE_PROFESIONAL
     * - horasDisponibles: horarios fijos del salón (8am a 5pm)
     * - historial: citas recientes de la clienta (se muestran en el panel lateral)
     *
     * Authentication auth: Spring Security inyecta automáticamente el usuario actual
     * para saber qué clienta está haciendo la reserva.
     */
    @GetMapping("/formulario")
    public String mostrarFormulario(Authentication auth, Model model) {
        // Crear una nueva reserva vacía para enlazar con el formulario Thymeleaf
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setEstado("PENDIENTE"); // todas las citas nuevas empiezan como pendientes

        // Obtener la clienta autenticada para mostrar su historial
        Usuario clienta = usuarioService.buscarPorCorreo(auth.getName());
        List<Reserva> historial = reservaService.listarHistorialCliente(clienta.getId(), null);

        // Pasar datos al template Thymeleaf
        model.addAttribute("reserva", nuevaReserva);
        model.addAttribute("clienta", clienta);
        model.addAttribute("servicios", servicioRepository.findByActivoTrue()); // solo servicios activos
        model.addAttribute("profesionales", usuarioRepository.findByRol("ROLE_PROFESIONAL"));
        model.addAttribute("horasDisponibles", List.of(
                "08:00", "09:00", "10:00", "11:00",
                "14:00", "15:00", "16:00", "17:00")); // horario del salón
        model.addAttribute("historial", historial);

        return "reservas/formulario";
    }

    /**
     * Guarda la nueva reserva en la base de datos.
     *
     * Recibe los datos del formulario enlazados en el objeto Reserva.
     * Asigna automáticamente:
     * - cliente: la clienta autenticada actualmente
     * - estado: PENDIENTE (todas las citas nuevas)
     *
     * Después de guardar, redirige al mismo formulario con ?exito=true
     * para mostrar el mensaje de confirmación.
     *
     * @param reserva Objeto Reserva con servicio, profesional, fecha y pago del form
     * @param auth    Usuario autenticado (Spring Security lo inyecta)
     */
    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute("reserva") Reserva reserva,
                                  Authentication auth) {
        // Asignar la clienta autenticada como dueña de la reserva
        Usuario clienta = usuarioService.buscarPorCorreo(auth.getName());
        reserva.setCliente(clienta);
        reserva.setEstado("PENDIENTE");

        // Guardar en la BD
        reservaService.guardarReserva(reserva);

        // Redirigir con parámetro de éxito para mostrar mensaje de confirmación
        return "redirect:/reservas/formulario?exito=true";
    }
}
