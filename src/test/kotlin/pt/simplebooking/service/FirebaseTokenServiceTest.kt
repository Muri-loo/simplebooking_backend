package pt.simplebooking.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FirebaseTokenServiceTest {

    private val service = FirebaseTokenService(mockk<FirebaseAuth>(relaxed = true))

    @Test
    fun shouldExtractTokenFromBearerHeader() {
        assertEquals("abc.def.ghi", service.extractBearerToken("Bearer abc.def.ghi"))
    }

    @Test
    fun shouldExtractTokenFromLowercaseBearerHeader() {
        assertEquals("abc.def.ghi", service.extractBearerToken("bearer abc.def.ghi"))
    }

    @Test
    fun shouldTrimWhitespaceAroundTheToken() {
        assertEquals("abc.def.ghi", service.extractBearerToken("Bearer   abc.def.ghi  "))
    }

    @Test
    fun shouldRejectAHeaderWithoutTheBearerScheme() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.extractBearerToken("Basic abc.def.ghi")
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("Authorization header must use the 'Bearer' scheme", ex.reason)
    }

    @Test
    fun shouldRejectABearerSchemeWithoutAToken() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.extractBearerToken("Bearer")
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("Authorization header must use the 'Bearer' scheme", ex.reason)
    }

    @Test
    fun shouldRejectAnEmptyBearerToken() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.extractBearerToken("Bearer    ")
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("Authorization header is missing the bearer token", ex.reason)
    }

    @Test
    fun shouldAcceptAnAdminToken() {
        service.verifyAdminRole(tokenWithClaims(mapOf("admin" to true)))
    }

    @Test
    fun shouldRejectATokenWithoutTheAdminClaim() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.verifyAdminRole(tokenWithClaims(emptyMap()))
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User does not have admin privileges", ex.reason)
    }

    @Test
    fun shouldRejectATokenWithAFalseAdminClaim() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.verifyAdminRole(tokenWithClaims(mapOf("admin" to false)))
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User does not have admin privileges", ex.reason)
    }

    @Test
    fun shouldRejectATokenWithANonBooleanAdminClaim() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.verifyAdminRole(tokenWithClaims(mapOf("admin" to "true")))
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User does not have admin privileges", ex.reason)
    }

    @Test
    fun shouldAcceptAMatchingBusinessOwnership() {
        service.verifyBusinessOwnership(
            tokenWithClaims(mapOf("businessId" to "business-123")),
            "business-123"
        )
    }

    @Test
    fun shouldRejectAMismatchedBusinessOwnership() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.verifyBusinessOwnership(
                tokenWithClaims(mapOf("businessId" to "business-456")),
                "business-123"
            )
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User does not own this business", ex.reason)
    }

    @Test
    fun shouldRejectATokenWithoutABusinessIdClaim() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.verifyBusinessOwnership(tokenWithClaims(emptyMap()), "business-123")
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("User does not own this business", ex.reason)
    }

    private fun tokenWithClaims(claims: Map<String, Any>): FirebaseToken {
        val token = mockk<FirebaseToken>()
        every { token.claims } returns claims
        return token
    }
}
