package co.edu.usco.reservas.controller;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.service.ReservaService;
import co.edu.usco.reservas.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ReservaService reservaService;
    private final LocaleResolver localeResolver;
    // MessageSource inyectado en el constructor para obtener
    // los nombres de meses y días de la semana traducidos al idioma activo
    private final MessageSource messageSource;

    public AuthController(UsuarioService usuarioService,
                          ReservaService reservaService,
                          LocaleResolver localeResolver,
                          MessageSource messageSource) {
        this.usuarioService   = usuarioService;
        this.reservaService   = reservaService;
        this.localeResolver   = localeResolver;
        this.messageSource    = messageSource;
    }

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "lang", required = false) String lang,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (lang != null && !lang.isBlank()) {
            Locale locale = switch (lang) {
                case "en" -> Locale.ENGLISH;
                case "pt" -> new Locale("pt");
                case "it" -> Locale.ITALIAN;
                default   -> new Locale("es");
            };
            localeResolver.setLocale(request, response, locale);
        }
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new co.edu.usco.reservas.entity.Usuario());
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
        usuario.setRol("ROLE_CLIENTE");
        usuarioService.registrarUsuario(usuario);
        return "redirect:/login?registro=true";
    }

    /**
     * Dashboard de la clienta.
     * El Locale se obtiene de la petición HTTP para resolver
     * los nombres de meses y días en el idioma activo.
     * Estos valores se pasan al template para que el calendario
     * JavaScript los use sin necesidad de lógica de idioma en JS.
     */
    @GetMapping("/cliente/dashboard")
    public String dashboardCliente(Authentication auth, Model model,
                                   HttpServletRequest request) {
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());

        // Obtener el Locale activo desde la petición HTTP
        // (CookieLocaleResolver lo resuelve automáticamente)
        Locale locale = localeResolver.resolveLocale(request);

        model.addAttribute("usuario",    usuario);
        model.addAttribute("historial",  reservaService.listarHistorialCliente(usuario.getId(), null));
        model.addAttribute("pendientes", reservaService.listarHistorialCliente(usuario.getId(), "PENDIENTE"));
        model.addAttribute("realizadas", reservaService.listarHistorialCliente(usuario.getId(), "REALIZADO"));

        // Nombres de meses en el idioma activo para el calendario JS
        // messageSource.getMessage resuelve la clave del archivo messages_XX.properties
        model.addAttribute("mesesCalendario", Arrays.asList(
                messageSource.getMessage("cal.enero",     null, locale),
                messageSource.getMessage("cal.febrero",   null, locale),
                messageSource.getMessage("cal.marzo",     null, locale),
                messageSource.getMessage("cal.abril",     null, locale),
                messageSource.getMessage("cal.mayo",      null, locale),
                messageSource.getMessage("cal.junio",     null, locale),
                messageSource.getMessage("cal.julio",     null, locale),
                messageSource.getMessage("cal.agosto",    null, locale),
                messageSource.getMessage("cal.septiembre",null, locale),
                messageSource.getMessage("cal.octubre",   null, locale),
                messageSource.getMessage("cal.noviembre", null, locale),
                messageSource.getMessage("cal.diciembre", null, locale)
        ));

        // Días de la semana para los encabezados del calendario
        model.addAttribute("diasSemana", Arrays.asList(
                messageSource.getMessage("cal.dom", null, locale),
                messageSource.getMessage("cal.lun", null, locale),
                messageSource.getMessage("cal.mar", null, locale),
                messageSource.getMessage("cal.mie", null, locale),
                messageSource.getMessage("cal.jue", null, locale),
                messageSource.getMessage("cal.vie", null, locale),
                messageSource.getMessage("cal.sab", null, locale)
        ));

        return "cliente/dashboard";
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());
        model.addAttribute("usuario",   usuario);
        model.addAttribute("historial", reservaService.listarHistorialCliente(usuario.getId(), null));
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute("usuario") Usuario datosNuevos,
                                   @RequestParam(required = false) String passwordNuevo,
                                   Authentication auth, Model model) {
        usuarioService.actualizarPerfil(auth.getName(), datosNuevos, passwordNuevo);
        Usuario usuario = usuarioService.buscarPorCorreo(auth.getName());
        model.addAttribute("exito",     true);
        model.addAttribute("usuario",   usuario);
        model.addAttribute("historial", reservaService.listarHistorialCliente(usuario.getId(), null));
        return "perfil";
    }
}