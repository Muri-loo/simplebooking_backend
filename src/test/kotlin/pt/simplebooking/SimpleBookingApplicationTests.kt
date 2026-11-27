package pt.simplebooking

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SimpleBookingApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var firebaseApp: FirebaseApp

    @MockitoBean
    private lateinit var firebaseAuth: FirebaseAuth

    @MockitoBean
    private lateinit var firestore: Firestore

    @Test
    fun contextLoads() {
        assertTrue(true)
    }

    @Test
    fun shouldAllowCorsPreflightFromConfiguredOrigin() {
        mockMvc.perform(
            options("/service-catalog/service-123")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization")
        )
            .andExpect(status().isOk)
            .andExpect(
                header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
            )
            .andExpect(
                header().string(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                    containsString("DELETE")
                )
            )
    }

    @Test
    fun shouldRejectCorsPreflightFromUnknownOrigin() {
        mockMvc.perform(
            options("/service-catalog/service-123")
                .header(HttpHeaders.ORIGIN, "https://malicious.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
        )
            .andExpect(status().isForbidden)
            .andExpect(
                header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
            )
    }

    private companion object {
        const val ALLOWED_ORIGIN = "http://localhost:5173"
    }
}
