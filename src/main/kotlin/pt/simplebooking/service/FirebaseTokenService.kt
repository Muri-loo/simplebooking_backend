package pt.simplebooking.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FirebaseTokenService(
    private val firebaseAuth: FirebaseAuth
) {

    fun verifyToken(idToken: String): FirebaseToken {
        return try {
            firebaseAuth.verifyIdToken(idToken)
        } catch (_: Exception) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid Firebase ID token"
            )
        }
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
}
