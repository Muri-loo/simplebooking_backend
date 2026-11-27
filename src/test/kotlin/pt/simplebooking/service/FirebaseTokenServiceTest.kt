package pt.simplebooking.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pt.simplebooking.error.BusinessAccessDeniedException
import pt.simplebooking.error.InvalidRequestException
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
        val ex = assertFailsWith<InvalidRequestException> {
            service.extractBearerToken("Basic abc.def.ghi")
        }

        assertEquals("Authorization header must use the 'Bearer' scheme", ex.message)
    }

    @Test
    fun shouldRejectABearerSchemeWithoutAToken() {
        val ex = assertFailsWith<InvalidRequestException> {
            service.extractBearerToken("Bearer")
        }

        assertEquals("Authorization header must use the 'Bearer' scheme", ex.message)
    }

    @Test
    fun shouldRejectAnEmptyBearerToken() {
        val ex = assertFailsWith<InvalidRequestException> {
            service.extractBearerToken("Bearer    ")
        }

        assertEquals("Authorization header is missing the bearer token", ex.message)
    }

    @Test
    fun shouldAcceptAnAdminToken() {
        service.verifyAdminRole(tokenWithClaims(mapOf("admin" to true)))
    }

    @Test
    fun shouldRejectATokenWithoutTheAdminClaim() {
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.verifyAdminRole(tokenWithClaims(emptyMap()))
        }

        assertEquals("User does not have admin privileges", ex.message)
    }

    @Test
    fun shouldRejectATokenWithAFalseAdminClaim() {
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.verifyAdminRole(tokenWithClaims(mapOf("admin" to false)))
        }

        assertEquals("User does not have admin privileges", ex.message)
    }

    @Test
    fun shouldRejectATokenWithANonBooleanAdminClaim() {
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.verifyAdminRole(tokenWithClaims(mapOf("admin" to "true")))
        }

        assertEquals("User does not have admin privileges", ex.message)
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
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.verifyBusinessOwnership(
                tokenWithClaims(mapOf("businessId" to "business-456")),
                "business-123"
            )
        }

        assertEquals("User does not own this business", ex.message)
    }

    @Test
    fun shouldRejectATokenWithoutABusinessIdClaim() {
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.verifyBusinessOwnership(tokenWithClaims(emptyMap()), "business-123")
        }

        assertEquals("User does not own this business", ex.message)
    }

    private fun tokenWithClaims(claims: Map<String, Any>): FirebaseToken {
        val token = mockk<FirebaseToken>()
        every { token.claims } returns claims
        return token
    }
}
