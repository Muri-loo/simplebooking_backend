package pt.simplebooking

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.simplebooking.config.TraceIdFilter

@SpringBootTest(
    properties = [
        "app.cors.allowed-origins=http://localhost:5173",
        "app.firebase.project-id=test-project",
        "app.logging.trace-project-id=test-project"
    ]
)
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension::class)
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
            options("/service-catalog/service-123").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization")
        ).andExpect(status().isOk).andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
        ).andExpect(
            header().string(
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("DELETE")
            )
        )
    }

    @Test
    fun shouldRejectCorsPreflightFromUnknownOrigin() {
        mockMvc.perform(
            options("/service-catalog/service-123").header(HttpHeaders.ORIGIN, "https://malicious.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
        ).andExpect(status().isForbidden).andExpect(
            header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
        )
    }

    @Test
    fun shouldExposeTraceIdFromCloudTraceContext() {
        mockMvc.perform(
            get("/unknown").header("X-Cloud-Trace-Context", "$TRACE_ID/$SPAN_ID;o=1")
        ).andExpect(header().string("X-Trace-Id", TRACE_ID))
    }

    @Test
    fun shouldExposeTraceIdFromTraceparent() {
        mockMvc.perform(
            get("/unknown").header("traceparent", "00-$TRACE_ID-$SPAN_ID-01")
        ).andExpect(header().string("X-Trace-Id", TRACE_ID))
    }

    @Test
    fun shouldPutGcpTraceFieldsInMdcForTheWholeRequest() {
        val request = MockHttpServletRequest().apply {
            addHeader("X-Cloud-Trace-Context", "$TRACE_ID/$SPAN_ID;o=1")
        }
        val response = MockHttpServletResponse()
        var traceInMdc: String? = null
        var spanInMdc: String? = null

        TraceIdFilter("test-project").doFilter(
            request, response
        ) { _, _ ->
            traceInMdc = MDC.get(TraceIdFilter.TRACE_MDC_KEY)
            spanInMdc = MDC.get(TraceIdFilter.SPAN_MDC_KEY)
        }

        assertEquals("projects/test-project/traces/$TRACE_ID", traceInMdc)
        assertEquals(SPAN_ID, spanInMdc)
        assertNull(MDC.get(TraceIdFilter.TRACE_MDC_KEY))
        assertNull(MDC.get(TraceIdFilter.SPAN_MDC_KEY))
    }

    @Test
    fun shouldWriteGcpSeverityNamesInStructuredLogs(output: CapturedOutput) {
        val logger = LoggerFactory.getLogger("structured-log-probe")

        logger.info("probe info")
        logger.warn("probe warn")

        val lines = output.all.lines().filter { it.contains("probe ") }
        assertTrue(lines.any { it.contains("\"severity\":\"INFO\"") })
        assertTrue(lines.any { it.contains("\"severity\":\"WARNING\"") })
    }

    private companion object {
        const val ALLOWED_ORIGIN = "http://localhost:5173"
        const val TRACE_ID = "a3f8762b574c508fc1739c88caf270f1"
        const val SPAN_ID = "c71fd6c8ce87fd22"
    }
}
