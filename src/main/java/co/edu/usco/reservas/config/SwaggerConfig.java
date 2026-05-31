package co.edu.usco.reservas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig — Configuración de la documentación API con OpenAPI 3.
 *
 * SpringDoc genera automáticamente la documentación de todos los
 * endpoints anotados con @RestController en el paquete /api.
 *
 * Acceso a la documentación:
 *   http://localhost:8080/swagger-ui/index.html
 *   https://localhost:8443/swagger-ui/index.html
 *
 * La documentación permite:
 * - Ver todos los endpoints disponibles de la API REST
 * - Probar los endpoints directamente desde el navegador
 * - Exportar la especificación en formato JSON (para Postman)
 *
 * Endpoints documentados automáticamente:
 * - ReservaApiController  → /api/reservas/**
 * - UsuarioApiController  → /api/usuarios/**
 *
 * Para importar en Postman:
 * 1. Abrir Postman → Import
 * 2. URL: http://localhost:8080/v3/api-docs
 * 3. Postman genera la colección automáticamente
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define los metadatos de la API que aparecen en la interfaz de Swagger.
     * Incluye título, descripción, versión, contacto e institución.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Girls Club - API de Reservas")
                        .description("API REST para el sistema de reservas del salón de uñas The Girls Club. "
                                + "Permite gestionar citas, especialistas y clientas desde herramientas externas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("The Girls Club")
                                .email("admin@thegirlsclub.com"))
                        .license(new License()
                                .name("Universidad Surcolombiana - USCO")
                                .url("https://www.usco.edu.co")));
    }
}
