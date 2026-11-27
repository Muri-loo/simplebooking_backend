package pt.simplebooking.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*parseAllowedOrigins().toTypedArray())
            .allowedMethods(*ALLOWED_METHODS)
            .allowedHeaders("*")
            .maxAge(MAX_AGE_SECONDS)
    }

    internal fun parseAllowedOrigins(): List<String> =
        allowedOrigins.split(",")
            .map(String::trim)
            .filter(String::isNotEmpty)

    private companion object {
        const val MAX_AGE_SECONDS = 3600L
        val ALLOWED_METHODS = arrayOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    }
}
