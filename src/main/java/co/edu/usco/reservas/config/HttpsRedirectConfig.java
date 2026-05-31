package co.edu.usco.reservas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * HttpsRedirectConfig — Redirige automáticamente HTTP a HTTPS.
 *
 * Cuando la aplicación corre con SSL activo en el puerto 8443,
 * este filtro garantiza que cualquier petición que llegue por HTTP
 * sea redirigida automáticamente al equivalente HTTPS.
 *
 * Ejemplo:
 *   http://localhost:8080/login  →  https://localhost:8443/login
 *
 * Implementación:
 * Usa OncePerRequestFilter — un filtro de Spring que se ejecuta exactamente
 * UNA vez por petición (evita ejecuciones duplicadas en cadenas de filtros).
 *
 * Por qué no TomcatServletWebServerFactory:
 * Spring Boot 4 con spring-boot-starter-webmvc no incluye la clase
 * TomcatServletWebServerFactory en el classpath principal.
 * El filtro OncePerRequestFilter es más compatible y no requiere dependencias extra.
 */
@Configuration
public class HttpsRedirectConfig {

    /**
     * Filtro de redirección HTTP → HTTPS.
     *
     * Se ejecuta en cada petición entrante.
     * Si la petición no es segura (HTTP), construye la URL HTTPS equivalente
     * y envía un redirect 302 al navegador.
     *
     * Si la petición ya es HTTPS (request.isSecure() = true),
     * simplemente continúa la cadena de filtros normalmente.
     */
    @Bean
    public OncePerRequestFilter httpsRedirectFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                if (!request.isSecure()) {
                    // Construir la URL HTTPS equivalente
                    // - getServerName(): "localhost"
                    // - getRequestURI(): "/login", "/admin/dashboard", etc.
                    String httpsUrl = "https://" + request.getServerName()
                            + ":8443" + request.getRequestURI();

                    // Preservar los parámetros de la URL original (?lang=en, ?error, etc.)
                    String query = request.getQueryString();
                    if (query != null) httpsUrl += "?" + query;

                    // Redirigir al navegador con código 302 (Found / Redirect temporal)
                    response.sendRedirect(httpsUrl);
                    return; // No continuar la cadena de filtros
                }

                // La petición ya es HTTPS — continuar normalmente
                filterChain.doFilter(request, response);
            }
        };
    }
}
