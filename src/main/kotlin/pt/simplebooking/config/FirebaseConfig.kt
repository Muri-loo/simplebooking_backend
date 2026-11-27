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
        if (projectId.isBlank()) {
            throw IllegalStateException(
                "FIREBASE_PROJECT_ID is required. Set it to the Firebase project.")
        }
    }

    @Bean(destroyMethod = "delete")
    fun firebaseApp(): FirebaseApp {
        logger.info("Firebase project ID: {}", projectId)
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
}
