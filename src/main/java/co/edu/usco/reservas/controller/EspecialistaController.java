package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.entity.Reserva;
import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.service.ReservaService;
import co.edu.usco.reservas.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/especialista")
public class EspecialistaController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    public EspecialistaController(ReservaService reservaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/agenda")
    public String verAgenda(Authentication auth, Model model) {
        Usuario profesional = usuarioService.buscarPorCorreo(auth.getName());

        List<Reserva> pendientes = reservaService.listarHistorialProfesional(profesional.getId(), "PENDIENTE");
        List<Reserva> realizadas = reservaService.listarHistorialProfesional(profesional.getId(), "REALIZADO");
        List<Reserva> todasMisCitas = reservaService.obtenerAgendaPorProfesional(profesional.getId());

        model.addAttribute("profesional", profesional);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("realizadas", realizadas);
        model.addAttribute("todasMisCitas", todasMisCitas);

        return "especialista/agenda";
    }

    @PostMapping("/marcar-atendida")
    public String marcarAtendida(@RequestParam Long reservaId, RedirectAttributes ra) {
        reservaService.cambiarEstadoReserva(reservaId, "REALIZADO");
        ra.addFlashAttribute("exito", "Cita marcada como atendida.");
        return "redirect:/especialista/agenda";
    }
}
