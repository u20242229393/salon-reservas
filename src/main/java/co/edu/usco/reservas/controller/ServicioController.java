package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.service.ServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServicioController {

    private final ServicioService servicioService;

    // Inyección de tu servicio ya corregido
    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping("/servicios")
    public String listarServicios(Model model) {
        // Con esto tu catálogo de "The Girls Club" listará los datos reales de la BD
        model.addAttribute("servicios", servicioService.listarServiciosActivos());
        return "servicios/lista"; // Abre tu nueva plantilla con breadcrumbs
    }
}