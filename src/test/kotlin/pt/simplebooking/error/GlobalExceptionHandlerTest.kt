package pt.simplebooking.error

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ExecutionException

class GlobalExceptionHandlerTest {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(ThrowingController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun shouldUnwrapExecutionExceptionAndKeepStatusAndMessage() {
        mockMvc.perform(get("/wrapped-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Service not found"))
            .andExpect(jsonPath("$.path").value("/wrapped-not-found"))
            .andExpect(jsonPath("$.trace").doesNotExist())
    }

    @Test
    fun shouldHideInternalErrorDetails() {
        mockMvc.perform(get("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.trace").doesNotExist())
    }

    @Test
    fun shouldReturnBadRequestWhenRequiredHeaderIsMissing() {
        mockMvc.perform(get("/requires-header"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.trace").doesNotExist())
    }
}

@RestController
class ThrowingController {

    @GetMapping("/wrapped-not-found")
    fun wrappedNotFound(): String {
        throw ExecutionException(
            ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found")
        )
    }

    @GetMapping("/boom")
    fun boom(): String {
        throw IllegalStateException("database exploded")
    }

    @GetMapping("/requires-header")
    fun requiresHeader(@RequestHeader("Authorization") authorization: String): String {
        return authorization
    }
}
