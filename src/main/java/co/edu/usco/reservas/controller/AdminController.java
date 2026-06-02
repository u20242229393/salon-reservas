package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.entity.Reserva;
import co.edu.usco.reservas.entity.Servicio;
import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.ReservaRepository;
import co.edu.usco.reservas.repository.ServicioRepository;
import co.edu.usco.reservas.repository.UsuarioRepository;
import co.edu.usco.reservas.service.ReservaService;
import co.edu.usco.reservas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ReservaService reservaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UsuarioService usuarioService;
    // Repository de servicios para gestion del catalogo
    @Autowired private ServicioRepository servicioRepository;

    // ── DASHBOARD ─────────────────────────────────────────────────────────
    /**
     * Muestra el panel principal de administracion.
     * Carga estadisticas, citas de hoy y recordatorios de cobro.
     * Las relaciones EAGER en Reserva.java garantizan que cliente,
     * profesional y servicio vengan cargados sin necesidad de queries extra.
     */
    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        List<Reserva> todas = reservaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        // Filtrar citas de hoy que no esten canceladas
        List<Reserva> citasHoy = todas.stream()
                .filter(r -> r.getFechaHoraCita() != null &&
                        r.getFechaHoraCita().toLocalDate().equals(hoy) &&
                        !"CANCELADO".equals(r.getEstado()))
                .collect(Collectors.toList());

        // Separar citas con pago presencial para mostrar recordatorios de cobro
        List<Reserva> pagoPresencial = citasHoy.stream()
                .filter(r -> "EFECTIVO".equals(r.getMetodoPago())
                        || "DATAFONO".equals(r.getMetodoPago())
                        || "TRANSFERENCIA".equals(r.getMetodoPago()))
                .collect(Collectors.toList());

        model.addAttribute("todasLasReservas", todas);
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("pagoPresencial", pagoPresencial);
        model.addAttribute("totalCitas", todas.size());
        model.addAttribute("citasPendientes", todas.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado())).count());

        return "admin/dashboard";
    }

    // ── AGENDA POR DIA ────────────────────────────────────────────────────
    /**
     * Permite a la administradora ver todas las citas de un dia especifico.
     * Si no se proporciona fecha, muestra las citas de hoy.
     * Las citas se ordenan por hora para facilitar la lectura.
     */
    @GetMapping("/agenda")
    public String verAgendaPorDia(
            @RequestParam(value = "fecha", required = false) String fechaStr,
            Model model) {

        // Si no viene fecha, usar la de hoy
        LocalDate fecha = (fechaStr != null && !fechaStr.isBlank())
                ? LocalDate.parse(fechaStr)
                : LocalDate.now();

        // Filtrar y ordenar citas del dia seleccionado
        List<Reserva> citasDelDia = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaHoraCita() != null
                        && r.getFechaHoraCita().toLocalDate().equals(fecha))
                .sorted(Comparator.comparing(Reserva::getFechaHoraCita))
                .collect(Collectors.toList());

        model.addAttribute("citasDelDia", citasDelDia);
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("totalDia", citasDelDia.size());

        return "admin/agenda";
    }

    // ── RESERVAS ──────────────────────────────────────────────────────────
    /**
     * Cancela una cita cambiando su estado a CANCELADO.
     * NO se elimina el registro para preservar el historial.
     */
    @PostMapping("/reserva/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = reservaService.cambiarEstadoReserva(id, "CANCELADO");
        ra.addFlashAttribute(ok ? "exito" : "error",
                ok ? "Cita cancelada correctamente." : "No se encontro la cita.");
        return "redirect:/admin/dashboard";
    }

    /**
     * Marca una cita como realizada.
     */
    @PostMapping("/reserva/{id}/realizado")
    public String marcarRealizado(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = reservaService.cambiarEstadoReserva(id, "REALIZADO");
        ra.addFlashAttribute(ok ? "exito" : "error",
                ok ? "Cita marcada como realizada." : "No se encontro la cita.");
        return "redirect:/admin/dashboard";
    }

    // ── GESTION DE ESPECIALISTAS ──────────────────────────────────────────
    @GetMapping("/especialistas")
    public String listarEspecialistas(Model model) {
        model.addAttribute("especialistas", usuarioRepository.findByRol("ROLE_PROFESIONAL"));
        model.addAttribute("nuevaEspecialista", new Usuario());
        return "admin/especialistas";
    }

    @PostMapping("/especialistas/crear")
    public String crearEspecialista(@ModelAttribute("nuevaEspecialista") Usuario usuario,
                                     RedirectAttributes ra) {
        if (usuarioService.existePorCorreo(usuario.getCorreo())) {
            ra.addFlashAttribute("error", "Ya existe una cuenta con ese correo.");
            return "redirect:/admin/especialistas";
        }
        usuario.setRol("ROLE_PROFESIONAL");
        usuarioService.registrarUsuario(usuario);
        ra.addFlashAttribute("exito", "Especialista " + usuario.getNombre() + " creada exitosamente.");
        return "redirect:/admin/especialistas";
    }

    @PostMapping("/especialistas/{id}/desactivar")
    public String desactivarEspecialista(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isPresent()) {
            Usuario u = opt.get();
            u.setActivo(!u.isActivo());
            usuarioRepository.save(u);
            ra.addFlashAttribute("exito",
                    u.isActivo() ? u.getNombre() + " activada." : u.getNombre() + " desactivada.");
        }
        return "redirect:/admin/especialistas";
    }

    @PostMapping("/especialistas/{id}/eliminar")
    public String eliminarEspecialista(@PathVariable Long id, RedirectAttributes ra) {
        usuarioRepository.deleteById(id);
        ra.addFlashAttribute("exito", "Especialista eliminada.");
        return "redirect:/admin/especialistas";
    }

    // ── GESTION DE SERVICIOS ──────────────────────────────────────────────
    /**
     * Lista todos los servicios (activos e inactivos).
     * Se muestran todos para que la admin pueda reactivar servicios
     * que habian sido desactivados previamente.
     */
    @GetMapping("/servicios")
    public String listarServicios(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        model.addAttribute("nuevoServicio", new Servicio());
        return "admin/servicios";
    }

    /**
     * Crea un nuevo servicio en el catalogo.
     * Acepta nombre, descripcion, duracion, precio e imagen de referencia.
     */
    @PostMapping("/servicios/crear")
    public String crearServicio(@ModelAttribute("nuevoServicio") Servicio servicio,
                                 RedirectAttributes ra) {
        // Los servicios nuevos siempre se crean activos
        servicio.setActivo(true);
        servicioRepository.save(servicio);
        ra.addFlashAttribute("exito",
                "Servicio '" + servicio.getNombre() + "' creado exitosamente.");
        return "redirect:/admin/servicios";
    }

    /**
     * Activa o desactiva un servicio (soft delete).
     * NO se elimina para preservar el historial de citas que usaron ese servicio.
     * Las citas pasadas siguen referenciando el servicio aunque este inactivo.
     */
    @PostMapping("/servicios/{id}/toggle")
    public String toggleServicio(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isPresent()) {
            Servicio s = opt.get();
            // Invertir estado: activo -> inactivo o inactivo -> activo
            s.setActivo(!s.isActivo());
            servicioRepository.save(s);
            ra.addFlashAttribute("exito",
                    s.getNombre() + (s.isActivo()
                            ? " activado correctamente."
                            : " desactivado. El historial de citas se conserva."));
        }
        return "redirect:/admin/servicios";
    }

    /**
     * Muestra el formulario de edición de un servicio existente.
     * Carga el servicio por ID y lo envía al modelo para prellenar el formulario.
     */
    @GetMapping("/servicios/{id}/editar")
    public String editarServicioForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Servicio no encontrado.");
            return "redirect:/admin/servicios";
        }
        model.addAttribute("servicios", servicioRepository.findAll());
        model.addAttribute("nuevoServicio", new Servicio());
        // El servicio a editar se envía separado para prellenar el formulario de edición
        model.addAttribute("servicioEditar", opt.get());
        return "admin/servicios";
    }

    /**
     * Guarda los cambios de un servicio editado.
     * Preserva el estado activo/inactivo actual del servicio.
     */
    @PostMapping("/servicios/{id}/editar")
    public String editarServicioGuardar(@PathVariable Long id,
                                        @ModelAttribute("servicioEditar") Servicio datos,
                                        RedirectAttributes ra) {
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isPresent()) {
            Servicio s = opt.get();
            // Actualizar solo los campos editables — el estado activo se preserva
            s.setNombre(datos.getNombre());
            s.setDescripcion(datos.getDescripcion());
            s.setDuracionMinutos(datos.getDuracionMinutos());
            s.setPrecio(datos.getPrecio());
            s.setImagenUrl(datos.getImagenUrl());
            servicioRepository.save(s);
            ra.addFlashAttribute("exito", "Servicio '" + s.getNombre() + "' actualizado correctamente.");
        }
        return "redirect:/admin/servicios";
    }
}
