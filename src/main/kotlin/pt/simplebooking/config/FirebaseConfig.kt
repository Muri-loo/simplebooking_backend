package pt.simplebooking.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.Firestore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfig(
    @Value("\${app.firebase.project-id}")
    private val projectId: String
) {

    private val logger = AppLogger.getLogger(FirebaseConfig::class.java)

    init {
        require(projectId.isNotBlank()) {
            "FIREBASE_PROJECT_ID is required."
        }

        require(environmentLabel(projectId) != "UNKNOWN") {
            "Unknown Firebase project ID: $projectId"
        }
    }

    @Bean(destroyMethod = "delete")
    fun firebaseApp(): FirebaseApp {
        logger.info("Firebase environment: {}", environmentLabel(projectId))
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .setProjectId(projectId)
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    @Bean
    fun firestore(firebaseApp: FirebaseApp): Firestore {
        return FirestoreClient.getFirestore(firebaseApp)
    }

    companion object {
        private const val PRODUCTION_PROJECT_ID = "barbershopbooking-ff17d"
        private const val TEST_PROJECT_ID = "simplebooking-ts"

        fun environmentLabel(projectId: String): String = when (projectId) {
            PRODUCTION_PROJECT_ID -> "PRODUCTION"
            TEST_PROJECT_ID -> "TEST"
            else -> "UNKNOWN"
        }
    }
}
