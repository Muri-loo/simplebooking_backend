package pt.simplebooking.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.Firestore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .setProjectId("barbershopbooking-ff17d")
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
