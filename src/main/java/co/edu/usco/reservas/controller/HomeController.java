package co.edu.usco.reservas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController — Controlador de páginas generales públicas.
 *
 * Gestiona las páginas que no requieren autenticación y no
 * pertenecen a ningún rol específico:
 * - Página de inicio (/)
 * - Página de contacto (/contactanos)
 * - Página de error 403 (/error/403)
 *
 * Estas URLs están marcadas como .permitAll() en SecurityConfig,
 * por lo que cualquier visitante puede accederlas sin iniciar sesión.
 */
@Controller
public class HomeController {

    /**
     * Página de inicio del salón.
     * Ruta: GET /
     * Muestra la landing page con información del salón y catálogo de servicios.
     */
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    /**
     * Página de contacto.
     * Ruta: GET /contactanos
     * Muestra la información de contacto, ubicación y mapa de Google Maps.
     */
    @GetMapping("/contactanos")
    public String contactanos() {
        return "contactanos";
    }

    /**
     * Página de error 403 — Acceso denegado.
     * Ruta: GET /error/403
     * Se muestra cuando un usuario autenticado intenta acceder a una URL
     * para la que no tiene el rol necesario.
     * Ejemplo: una clienta intentando acceder a /admin/dashboard.
     */
    @GetMapping("/error/403")
    public String error403() {
        return "error/403";
    }
}
