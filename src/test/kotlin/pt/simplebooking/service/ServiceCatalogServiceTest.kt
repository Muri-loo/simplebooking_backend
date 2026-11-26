
package pt.simplebooking.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore as FirestoreClient
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.Transaction
import com.google.firebase.auth.FirebaseToken
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.simplebooking.config.Firestore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceCatalogServiceTest {

    private lateinit var service: ServiceCatalogService

    private val mockFirebaseTokenService = mockk<FirebaseTokenService>(relaxed = true)
    private val mockFirestore = mockk<FirestoreClient>()
    private val mockDecodedToken = mockk<FirebaseToken>()

    private val resourceCollection = mockk<CollectionReference>()
    private val resourceQuery = mockk<Query>()
    private val resourceFuture = mockk<ApiFuture<QuerySnapshot>>()
    private val resourceSnapshot = mockk<QuerySnapshot>()
    private val resourceDocument = mockk<QueryDocumentSnapshot>()

    private val catalogCollection = mockk<CollectionReference>()
    private val catalogReference = mockk<DocumentReference>()
    private val catalogFuture = mockk<ApiFuture<DocumentSnapshot>>()
    private val catalogSnapshot = mockk<DocumentSnapshot>()

    private val transaction = mockk<Transaction>()
    private val transactionFuture = mockk<ApiFuture<Transaction>>()
    private val updateFields = slot<Map<String, Any>>()

    private val existingServices = listOf(
        mapOf<String, Any>(
            "id" to "service-1",
            "name" to "Haircut",
            "price" to 13
        ),
        mapOf<String, Any>(
            "id" to "service-2",
            "name" to "Beard",
            "price" to 8
        )
    )

    private val expectedServices = listOf(
        mapOf<String, Any>(
            "id" to "service-2",
            "name" to "Beard",
            "price" to 8
        )
    )

    @BeforeEach
    fun setup() {
        service = ServiceCatalogService(
            firebaseTokenService = mockFirebaseTokenService,
            firestore = mockFirestore
        )

        mockAuthentication()
        mockBookableResourceLookup()
        mockServiceCatalogLookup()
        mockTransaction()
    }

    @Test
    fun shouldDeleteOnlyRequestedService() {
        // Act
        service.deleteServiceCatalog(
            serviceId = SERVICE_ID_TO_DELETE,
            idToken = VALID_TOKEN
        )

        // Assert
        assertEquals(
            expectedServices,
            updateFields.captured[Firestore.Fields.ServiceCatalog.SERVICES]
        )

        assertTrue(
            updateFields.captured[Firestore.Fields.ServiceCatalog.UPDATED_AT] is FieldValue
        )

        verify(exactly = 1) {
            mockFirebaseTokenService.verifyBusinessOwnership(
                mockDecodedToken,
                BUSINESS_ID
            )
        }

        verify(exactly = 1) {
            transaction.update(catalogReference, any<Map<String, Any>>())
        }
    }

    private fun mockAuthentication() {
        every {
            mockFirebaseTokenService.verifyToken(VALID_TOKEN)
        } returns mockDecodedToken

        every { mockDecodedToken.uid } returns USER_ID

        every {
            mockFirebaseTokenService.verifyAdminRole(mockDecodedToken)
        } just runs

        every {
            mockFirebaseTokenService.verifyBusinessOwnership(
                mockDecodedToken,
                BUSINESS_ID
            )
        } just runs
    }

    private fun mockBookableResourceLookup() {
        every {
            mockFirestore.collection(Firestore.Collections.BOOKABLE_RESOURCES)
        } returns resourceCollection

        every {
            resourceCollection.whereEqualTo(
                Firestore.Fields.BookableResource.UID,
                USER_ID
            )
        } returns resourceQuery

        every { resourceQuery.limit(1) } returns resourceQuery
        every { resourceQuery.get() } returns resourceFuture
        every { resourceFuture.get() } returns resourceSnapshot
        every { resourceSnapshot.documents } returns listOf(resourceDocument)

        every {
            resourceDocument.getString(
                Firestore.Fields.BookableResource.BUSINESS_ID
            )
        } returns BUSINESS_ID
    }

    private fun mockServiceCatalogLookup() {
        every {
            mockFirestore.collection(Firestore.Collections.SERVICE_CATALOGS)
        } returns catalogCollection

        every {
            catalogCollection.document(BUSINESS_ID)
        } returns catalogReference

        every { catalogSnapshot.exists() } returns true

        every {
            catalogSnapshot.getString(
                Firestore.Fields.ServiceCatalog.BUSINESS_ID
            )
        } returns BUSINESS_ID

        every {
            catalogSnapshot.get(Firestore.Fields.ServiceCatalog.SERVICES)
        } returns existingServices
    }

    private fun mockTransaction() {
        every {
            transaction.get(catalogReference)
        } returns catalogFuture

        every { catalogFuture.get() } returns catalogSnapshot

        every {
            transaction.update(catalogReference, capture(updateFields))
        } returns transaction

        every { transactionFuture.get() } returns transaction

        every {
            mockFirestore.runTransaction<Transaction>(any())
        } answers {
            firstArg<Transaction.Function<Transaction>>()
                .updateCallback(transaction)

            transactionFuture
        }
    }

    private companion object {
        const val BUSINESS_ID = "business-123"
        const val USER_ID = "user-123"
        const val SERVICE_ID_TO_DELETE = "service-1"
        const val VALID_TOKEN = "valid-token"
    }
}
