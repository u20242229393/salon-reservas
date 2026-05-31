package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.service.ReservaService;
import co.edu.usco.reservas.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ReservaService reservaService;
    // LocaleResolver permite guardar el idioma elegido en la sesion HTTP
    // para que persista entre paginas y redirecciones
    private final LocaleResolver localeResolver;

    public AuthController(UsuarioService usuarioService,
                          ReservaService reservaService,
                          LocaleResolver localeResolver) {
        this.usuarioService = usuarioService;
        this.reservaService = reservaService;
        this.localeResolver = localeResolver;
    }

    /**
     * Muestra la pagina de login.
     * Si viene el parametro ?lang=XX, guarda el idioma en la sesion
     * para que persista incluso despues del redirect que hace Spring Security
     * al autenticar exitosamente.
     */
    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "lang", required = false) String lang,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (lang != null && !lang.isBlank()) {
            // Convertimos el codigo de idioma a un objeto Locale
            Locale locale = switch (lang) {
                case "en" -> Locale.ENGLISH;
                case "pt" -> new Locale("pt");
                case "it" -> Locale.ITALIAN;
                default   -> new Locale("es");
            };
            // Guardamos el Locale en la sesion HTTP
            // SessionLocaleResolver lo mantendra para todas las paginas siguientes
            localeResolver.setLocale(request, response, locale);
        }
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro/formulario";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute("usuario") Usuario usuario, Model model) {
        if (usuarioService.existePorCorreo(usuario.getCorreo())) {
            model.addAttribute("errorCorreo", "Este correo ya esta registrado.");
            return "registro/formulario";
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 8) {
            model.addAttribute("errorPassword", "La contrasena debe tener al menos 8 caracteres.");
            return "registro/formulario";
        }
        // Las clientas siempre se registran con rol ROLE_CLIENTE
        usuario.setRol("ROLE_CLIENTE");
        usuarioService.registrarUsuario(usuario);
        return "redirect:/login?registro=true";
    }

    /**
     * Dashboard principal de la clienta.
     * Carga el historial completo, citas pendientes y realizadas
     * para mostrar en el calendario interactivo.
     */
    @GetMapping("/cliente/dashboard")
    public String dashboardCliente(Authentication auth, Model model) {
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("historial",  reservaService.listarHistorialCliente(usuario.getId(), null));
        model.addAttribute("pendientes", reservaService.listarHistorialCliente(usuario.getId(), "PENDIENTE"));
        model.addAttribute("realizadas", reservaService.listarHistorialCliente(usuario.getId(), "REALIZADO"));
        return "cliente/dashboard";
    }

    /**
     * Pagina de perfil de la clienta.
     * Muestra datos personales e historial de citas.
     */
    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("historial", reservaService.listarHistorialCliente(usuario.getId(), null));
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute("usuario") Usuario datosNuevos,
                                   @RequestParam(required = false) String passwordNuevo,
                                   Authentication auth, Model model) {
        usuarioService.actualizarPerfil(auth.getName(), datosNuevos, passwordNuevo);
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());
        model.addAttribute("exito", true);
        model.addAttribute("usuario", usuario);
        model.addAttribute("historial", reservaService.listarHistorialCliente(usuario.getId(), null));
        return "perfil";
    }
}
