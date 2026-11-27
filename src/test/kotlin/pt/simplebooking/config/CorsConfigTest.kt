package pt.simplebooking.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CorsConfigTest {

    @Test
    fun shouldParseCommaSeparatedOriginsAndTrimWhitespace() {
        val origins = CorsConfig("https://staging.simplebooking.pt, https://ts.simplebooking.pt")
            .parseAllowedOrigins()

        assertEquals(
            listOf("https://staging.simplebooking.pt", "https://ts.simplebooking.pt"),
            origins
        )
    }

    @Test
    fun shouldIgnoreBlankOrigins() {
        val origins = CorsConfig("https://simplebooking.pt, ,")
            .parseAllowedOrigins()

        assertEquals(listOf("https://simplebooking.pt"), origins)
    }
}
