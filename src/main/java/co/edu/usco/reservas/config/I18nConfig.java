package co.edu.usco.reservas.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

/**
 * I18nConfig — Configuración de internacionalización (i18n).
 *
 * Permite que la aplicación muestre textos en múltiples idiomas:
 *   Español (es) — por defecto
 *   Inglés  (en)
 *   Portugués (pt)
 *   Italiano  (it)
 *
 * Cómo funciona el flujo completo:
 * 1. Usuario selecciona un idioma en el selector 🌐 del navbar
 * 2. El navegador navega a la URL actual con ?lang=en (o pt, it, es)
 * 3. LocaleChangeInterceptor captura el parámetro ?lang
 * 4. SessionLocaleResolver guarda el Locale en la sesión HTTP
 * 5. En las plantillas Thymeleaf, #{nav.inicio} busca la clave "nav.inicio"
 *    en el archivo messages_en.properties (si el idioma es inglés)
 * 6. El idioma persiste para toda la sesión hasta que se cambie o cierre sesión
 *
 * Archivos de mensajes (en src/main/resources/i18n/):
 *   messages.properties      → Español (por defecto)
 *   messages_en.properties   → Inglés
 *   messages_pt.properties   → Portugués
 *   messages_it.properties   → Italiano
 *
 * Implementa WebMvcConfigurer para registrar el interceptor de cambio de idioma.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * MessageSource — origen de los mensajes i18n.
     *
     * ReloadableResourceBundleMessageSource permite recargar los archivos
     * de mensajes sin reiniciar la aplicación (útil en desarrollo).
     *
     * basename: prefijo de los archivos de mensajes.
     * Spring busca automáticamente messages.properties, messages_en.properties, etc.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        // Si no encuentra una clave, muestra la clave misma (facilita depuración)
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    /**
     * LocaleResolver — decide cómo se determina el idioma de cada petición.
     *
     * SessionLocaleResolver almacena el Locale elegido en la sesión HTTP.
     * Esto significa que el idioma persiste mientras la sesión esté activa,
     * incluso al navegar entre páginas.
     *
     * Alternativa no usada: CookieLocaleResolver (guarda en cookie del navegador,
     * persiste entre sesiones pero es más complejo de manejar con OAuth2).
     *
     * Idioma por defecto: Español ("es")
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("es"));
        return resolver;
    }

    /**
     * LocaleChangeInterceptor — intercepta peticiones con el parámetro ?lang=XX.
     *
     * Funciona como un filtro que revisa cada petición HTTP.
     * Si encuentra el parámetro "lang" en la URL, llama a LocaleResolver
     * para cambiar y guardar el nuevo idioma en la sesión.
     *
     * Ejemplo: GET /cliente/dashboard?lang=en
     * → El interceptor detecta lang=en
     * → Llama a SessionLocaleResolver.setLocale(request, response, Locale.ENGLISH)
     * → El dashboard se renderiza en inglés
     * → Las siguientes páginas también serán en inglés
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // nombre del parámetro en la URL
        return interceptor;
    }

    /**
     * Registra el interceptor en el pipeline de Spring MVC.
     * Sin este registro, el interceptor existe pero no se ejecuta.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
