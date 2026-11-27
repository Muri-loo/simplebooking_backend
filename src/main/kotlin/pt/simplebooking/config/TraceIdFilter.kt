package pt.simplebooking.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TraceIdFilter(
    @Value("\${app.logging.trace-project-id}")
    private val traceProjectId: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val context = resolveTraceContext(request)

        if (context != null) {
            MDC.put(TRACE_MDC_KEY, "projects/$traceProjectId/traces/${context.traceId}")
            context.spanId?.let { MDC.put(SPAN_MDC_KEY, it) }
            response.setHeader(TRACE_HEADER, context.traceId)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_MDC_KEY)
            MDC.remove(SPAN_MDC_KEY)
        }
    }

    private fun resolveTraceContext(request: HttpServletRequest): TraceContext? =
        parseCloudTrace(request.getHeader(CLOUD_TRACE_HEADER))
            ?: parseTraceparent(request.getHeader(TRACEPARENT_HEADER))

    private fun parseCloudTrace(header: String?): TraceContext? {
        if (header.isNullOrBlank()) return null

        val traceId = header.substringBefore('/').trim()
        val spanId = header.substringAfter('/', "").substringBefore(';').trim()

        return if (traceId.isEmpty()) {
            null
        } else {
            TraceContext(traceId, spanId.ifEmpty { null })
        }
    }

    private fun parseTraceparent(header: String?): TraceContext? {
        val parts = header?.split('-')

        return if (
            parts == null ||
            parts.size < TRACEPARENT_MIN_PARTS ||
            parts[1].isEmpty()
        ) {
            null
        } else {
            TraceContext(parts[1], parts[2].ifEmpty { null })
        }
    }

    private data class TraceContext(val traceId: String, val spanId: String?)

    companion object {
        const val TRACE_HEADER = "X-Trace-Id"
        const val TRACE_MDC_KEY = "logging.googleapis.com/trace"
        const val SPAN_MDC_KEY = "logging.googleapis.com/spanId"
        private const val CLOUD_TRACE_HEADER = "X-Cloud-Trace-Context"
        private const val TRACEPARENT_HEADER = "traceparent"
        private const val TRACEPARENT_MIN_PARTS = 4
    }
}
