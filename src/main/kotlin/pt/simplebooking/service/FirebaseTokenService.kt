package pt.simplebooking.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FirebaseTokenService(
    private val firebaseAuth: FirebaseAuth
) {

    fun extractBearerToken(authHeader: String): String {
        if (!authHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Authorization header must use the 'Bearer' scheme"
            )
        }

        val idToken = authHeader.substring(BEARER_PREFIX.length).trim()

        if (idToken.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Authorization header is missing the bearer token"
            )
        }

        return idToken
    }

    fun verifyToken(idToken: String): FirebaseToken {
        return try {
            firebaseAuth.verifyIdToken(idToken)
        } catch (ex: FirebaseAuthException) {
            throw invalidToken(ex)
        } catch (ex: IllegalArgumentException) {
            throw invalidToken(ex)
        }
    }

    private fun invalidToken(cause: Exception): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid Firebase ID token",
            cause
        )
    }

    fun verifyAdminRole(decodedToken: FirebaseToken) {
        val isAdmin = decodedToken.claims["admin"] as? Boolean ?: false

        if (isAdmin.not()) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User does not have admin privileges"
            )
        }
    }

    fun verifyBusinessOwnership(
        decodedToken: FirebaseToken,
        businessId: String
    ) {
        val tokenBusinessId = decodedToken.claims["businessId"] as? String

        if (tokenBusinessId != businessId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User does not own this business"
            )
        }
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}
