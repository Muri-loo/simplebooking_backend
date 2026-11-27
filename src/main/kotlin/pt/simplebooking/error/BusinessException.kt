package pt.simplebooking.error

/**
 * Base type for failures caused by the caller's request.
 *
 * These exceptions carry no transport details; [GlobalExceptionHandler] is
 * responsible for translating them into HTTP responses.
 */
sealed class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class InvalidRequestException(message: String) : BusinessException(message)

class AuthenticationFailedException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

class BusinessAccessDeniedException(message: String) : BusinessException(message)

class BusinessResourceNotFoundException(message: String) : BusinessException(message)
