package pt.simplebooking.service

import com.google.firebase.auth.FirebaseAuth
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
}
