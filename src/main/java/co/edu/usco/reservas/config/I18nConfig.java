package co.edu.usco.reservas.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * I18nConfig — Configuración de internacionalización (i18n).
 *
 * Permite que la aplicación muestre textos en 4 idiomas:
 *   Español (es) — por defecto
 *   Inglés  (en)
 *   Portugués (pt)
 *   Italiano  (it)
 *
 * DECISIÓN DE DISEÑO — CookieLocaleResolver en lugar de SessionLocaleResolver:
 *
 * Problema con SessionLocaleResolver:
 * Cuando el usuario hace login, Spring Security invalida la sesión HTTP
 * anterior por seguridad (protección contra Session Fixation Attacks).
 * Esto borraba el idioma elegido porque estaba guardado en la sesión.
 *
 * Solución con CookieLocaleResolver:
 * El idioma se guarda en una COOKIE del navegador (no en la sesión del servidor).
 * Las cookies sobreviven al login porque viven en el navegador.
 * El idioma persiste incluso entre sesiones y reinicios del servidor.
 *
 * Flujo completo:
 * 1. Usuario hace clic en 🇺🇸 EN en el footer
 * 2. Navegador solicita la URL actual con ?lang=en
 * 3. LocaleChangeInterceptor captura el parámetro ?lang=en
 * 4. CookieLocaleResolver crea/actualiza la cookie "lang=en" (dura 1 año)
 * 5. Usuario hace login → Spring Security invalida la sesión (normal)
 * 6. En la siguiente petición, CookieLocaleResolver lee la cookie "lang=en"
 * 7. El dashboard se renderiza en inglés ✓
 *
 * Archivos de mensajes en src/main/resources/i18n/:
 *   messages.properties      → Español (por defecto)
 *   messages_en.properties   → Inglés
 *   messages_pt.properties   → Portugués
 *   messages_it.properties   → Italiano
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * MessageSource — carga y provee los textos traducidos.
     *
     * ReloadableResourceBundleMessageSource busca los archivos
     * messages_XX.properties según el idioma activo.
     * Permite recargar traducciones sin reiniciar la app (útil en desarrollo).
     * setUseCodeAsDefaultMessage: si no encuentra una clave, muestra la clave
     * misma en lugar de lanzar excepción (facilita detectar traducciones faltantes).
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    /**
     * LocaleResolver — determina el idioma de cada petición usando una cookie.
     *
     * CookieLocaleResolver lee y escribe una cookie llamada "lang" en el navegador.
     * Configuración:
     * - Nombre de la cookie: "lang"
     * - Duración: 365 días (1 año) — el idioma persiste entre sesiones
     * - Idioma por defecto: Español ("es") si no hay cookie
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setDefaultLocale(new Locale("es"));
        return resolver;
    }

    /**
     * LocaleChangeInterceptor — intercepta el parámetro ?lang=XX en cada URL.
     *
     * Se ejecuta antes de cada petición HTTP.
     * Si detecta el parámetro "lang", llama a CookieLocaleResolver
     * para actualizar la cookie con el nuevo idioma elegido.
     *
     * Ejemplo: GET /cliente/dashboard?lang=en
     * → Interceptor detecta lang=en
     * → CookieLocaleResolver actualiza cookie "lang=en"
     * → Thymeleaf usa messages_en.properties para renderizar la página
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Registra el interceptor en el pipeline de Spring MVC.
     * Sin este registro el interceptor existe como Bean pero no se ejecuta.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}