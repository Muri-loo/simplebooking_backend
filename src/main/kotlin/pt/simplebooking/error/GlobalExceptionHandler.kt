package pt.simplebooking.error

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import pt.simplebooking.config.AppLogger
import java.time.Instant
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = AppLogger.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val cause = unwrap(exception)
        val status = resolveStatus(cause)
        val message = resolveMessage(cause, status)

        if (status.is5xxServerError) {
            logger.error(
                "Unhandled exception for {} {}",
                request.method,
                request.requestURI,
                cause
            )
        } else {
            logRejectedRequest(request, message, cause)
        }

        return ResponseEntity
            .status(status)
            .headers(resolveHeaders(cause))
            .body(
                ApiError(
                    timestamp = Instant.now(),
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = request.requestURI
                )
            )
    }

    private fun logRejectedRequest(
        request: HttpServletRequest,
        message: String,
        cause: Throwable
    ) {
        val rootCause = cause.cause

        if (rootCause == null) {
            logger.warn(
                "Request rejected for {} {}: {}",
                request.method,
                request.requestURI,
                message
            )
        } else {
            logger.warn(
                "Request rejected for {} {}: {} (cause: {})",
                request.method,
                request.requestURI,
                message,
                rootCause.message
            )
        }
    }

    private fun unwrap(exception: Throwable): Throwable {
        var cause = exception

        while (
            (cause is ExecutionException || cause is CompletionException) &&
            cause.cause != null
        ) {
            cause = cause.cause!!
        }

        return cause
    }

    private fun resolveHeaders(cause: Throwable): HttpHeaders {
        return when (cause) {
            is ResponseStatusException -> cause.headers
            is ErrorResponse -> cause.headers
            else -> HttpHeaders()
        }
    }

    private fun resolveStatus(cause: Throwable): HttpStatus {
        return when (cause) {
            is ResponseStatusException -> toHttpStatus(cause.statusCode)
            is ErrorResponse -> toHttpStatus(cause.statusCode)
            is HttpMessageNotReadableException -> HttpStatus.BAD_REQUEST
            is MethodArgumentTypeMismatchException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun resolveMessage(cause: Throwable, status: HttpStatus): String {
        if (status.is5xxServerError) {
            return "An unexpected error occurred"
        }

        return when (cause) {
            is ResponseStatusException -> cause.reason ?: status.reasonPhrase
            is ErrorResponse -> cause.body.detail ?: status.reasonPhrase
            is HttpMessageNotReadableException -> "Malformed request body"
            is MethodArgumentTypeMismatchException -> "Invalid request parameter"
            else -> cause.message ?: status.reasonPhrase
        }
    }

    private fun toHttpStatus(statusCode: HttpStatusCode): HttpStatus {
        return HttpStatus.resolve(statusCode.value())
            ?: HttpStatus.INTERNAL_SERVER_ERROR
    }
}
