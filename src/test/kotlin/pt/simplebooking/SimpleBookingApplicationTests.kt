package pt.simplebooking

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class SimpleBookingApplicationTests {

    @MockitoBean
    private lateinit var firebaseApp: FirebaseApp

    @MockitoBean
    private lateinit var firebaseAuth: FirebaseAuth

    @MockitoBean
    private lateinit var firestore: Firestore

    @Test
    fun contextLoads() {
        assertTrue(true)
    }

}
