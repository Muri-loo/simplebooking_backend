package pt.simplebooking.config

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FirebaseConfigTest {

    @Test
    fun shouldFailFastWhenProjectIdIsMissing() {
        val error = assertThrows<IllegalStateException> { FirebaseConfig("") }

        assertTrue(error.message!!.contains("FIREBASE_PROJECT_ID is required"))
    }

    @Test
    fun shouldFailFastWhenProjectIdIsBlank() {
        assertThrows<IllegalStateException> { FirebaseConfig("   ") }
    }

    @Test
    fun shouldAcceptAnExplicitProjectId() {
        FirebaseConfig("barbershopbooking-ff17d")
    }
}
