package pt.simplebooking.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FirebaseConfigTest {

    @Test
    fun shouldFailFastWhenProjectIdIsMissing() {
        val error = assertThrows<IllegalArgumentException> { FirebaseConfig("") }

        assertTrue(error.message!!.contains("FIREBASE_PROJECT_ID is required"))
    }

    @Test
    fun shouldFailFastWhenProjectIdIsBlank() {
        assertThrows<IllegalArgumentException> { FirebaseConfig("   ") }
    }

    @Test
    fun shouldFailFastWhenProjectIdIsUnknown() {
        val error = assertThrows<IllegalArgumentException> { FirebaseConfig("sadasd") }

        assertTrue(error.message!!.contains("Unknown Firebase project ID: sadasd"))
    }

    @Test
    fun shouldAcceptKnownProjects() {
        FirebaseConfig("simplebooking-ts")
        FirebaseConfig("barbershopbooking-ff17d")
    }

    @Test
    fun shouldLabelKnownProjects() {
        assertEquals("TEST", FirebaseConfig.environmentLabel("simplebooking-ts"))
        assertEquals("PRODUCTION", FirebaseConfig.environmentLabel("barbershopbooking-ff17d"))
        assertEquals("UNKNOWN", FirebaseConfig.environmentLabel("sadasd"))
    }
}
