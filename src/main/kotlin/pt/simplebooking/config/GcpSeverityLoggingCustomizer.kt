package pt.simplebooking.config

import ch.qos.logback.classic.spi.ILoggingEvent
import org.springframework.boot.json.JsonWriter
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer

/**
 * Cloud Logging only understands the LogSeverity enum names, where the warning
 * level is "WARNING" (SLF4J uses "WARN"). Without this mapping the entry is
 * ingested as DEFAULT and severity filters hide it.
 */
class GcpSeverityLoggingCustomizer : StructuredLoggingJsonMembersCustomizer<ILoggingEvent> {

    override fun customize(members: JsonWriter.Members<ILoggingEvent>) {
        members.add("severity") { event -> GCP_SEVERITIES[event.level] ?: event.level.toString() }
    }

    private companion object {
        val GCP_SEVERITIES = mapOf(
            ch.qos.logback.classic.Level.TRACE to "DEBUG",
            ch.qos.logback.classic.Level.DEBUG to "DEBUG",
            ch.qos.logback.classic.Level.INFO to "INFO",
            ch.qos.logback.classic.Level.WARN to "WARNING",
            ch.qos.logback.classic.Level.ERROR to "ERROR"
        )
    }
}
