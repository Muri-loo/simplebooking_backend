package pt.simplebooking.repository

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.simplebooking.config.Firestore as FirestoreConstants
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ServiceCatalogRepositoryTest {

    private lateinit var repository: ServiceCatalogRepository

    private val mockFirestore = mockk<Firestore>()
    private val catalogCollection = mockk<CollectionReference>()
    private val catalogReference = mockk<DocumentReference>()
    private val catalogFuture = mockk<ApiFuture<DocumentSnapshot>>()
    private val catalogSnapshot = mockk<DocumentSnapshot>()
    private val transaction = mockk<Transaction>()
    private val transactionResultFuture = mockk<ApiFuture<RemoveServiceResult>>()
    private val transactionResult = slot<RemoveServiceResult>()
    private val updateFields = slot<Map<String, Any>>()

    @BeforeEach
    fun setup() {
        repository = ServiceCatalogRepository(mockFirestore)

        every {
            mockFirestore.collection(FirestoreConstants.Collections.SERVICE_CATALOGS)
        } returns catalogCollection

        every {
            catalogCollection.document(BUSINESS_ID)
        } returns catalogReference

        every { transaction.get(catalogReference) } returns catalogFuture
        every { catalogFuture.get() } returns catalogSnapshot

        every {
            transaction.update(catalogReference, capture(updateFields))
        } returns transaction

        every { transactionResultFuture.get() } answers {
            transactionResult.captured
        }

        every {
            mockFirestore.runTransaction<RemoveServiceResult>(any())
        } answers {
            transactionResult.captured = firstArg<
                Transaction.Function<RemoveServiceResult>
                >().updateCallback(transaction)

            transactionResultFuture
        }

        every { catalogSnapshot.exists() } returns true

        every {
            catalogSnapshot.getString(
                FirestoreConstants.Fields.ServiceCatalog.BUSINESS_ID
            )
        } returns BUSINESS_ID

        every {
            catalogSnapshot.get(FirestoreConstants.Fields.ServiceCatalog.SERVICES)
        } returns existingServices
    }

    @Test
    fun shouldRemoveRequestedServiceAndUpdateTimestamp() {
        // Act
        val result = repository.removeService(BUSINESS_ID, SERVICE_ID)

        // Assert
        assertEquals(RemoveServiceResult.Removed, result)
        assertEquals(
            expectedServices,
            updateFields.captured[FirestoreConstants.Fields.ServiceCatalog.SERVICES]
        )
        assertTrue(
            updateFields.captured[FirestoreConstants.Fields.ServiceCatalog.UPDATED_AT] is FieldValue
        )
        verify(exactly = 1) {
            transaction.update(catalogReference, any<Map<String, Any>>())
        }
    }

    @Test
    fun shouldReturnCatalogNotFoundWhenDocumentDoesNotExist() {
        every { catalogSnapshot.exists() } returns false

        val result = repository.removeService(BUSINESS_ID, SERVICE_ID)

        assertEquals(RemoveServiceResult.CatalogNotFound, result)
        verify(exactly = 0) {
            transaction.update(any(), any<Map<String, Any>>())
        }
    }

    @Test
    fun shouldReturnBusinessMismatchWhenCatalogBelongsToAnotherBusiness() {
        every {
            catalogSnapshot.getString(
                FirestoreConstants.Fields.ServiceCatalog.BUSINESS_ID
            )
        } returns "other-business"

        val result = repository.removeService(BUSINESS_ID, SERVICE_ID)

        assertEquals(RemoveServiceResult.BusinessMismatch, result)
        verify(exactly = 0) {
            transaction.update(any(), any<Map<String, Any>>())
        }
    }

    @Test
    fun shouldReturnServiceNotFoundWhenServiceIsNotInCatalog() {
        val result = repository.removeService(BUSINESS_ID, "missing-service")

        assertEquals(RemoveServiceResult.ServiceNotFound, result)
        verify(exactly = 0) {
            transaction.update(any(), any<Map<String, Any>>())
        }
    }

    @Test
    fun shouldReturnInvalidCatalogWhenServicesAreNotAList() {
        every {
            catalogSnapshot.get(FirestoreConstants.Fields.ServiceCatalog.SERVICES)
        } returns "not-a-list"

        val result = repository.removeService(BUSINESS_ID, SERVICE_ID)

        assertIs<RemoveServiceResult.InvalidCatalog>(result)
        verify(exactly = 0) {
            transaction.update(any(), any<Map<String, Any>>())
        }
    }

    private companion object {
        const val BUSINESS_ID = "business-123"
        const val SERVICE_ID = "service-1"

        val existingServices = listOf(
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

        val expectedServices = listOf(
            mapOf<String, Any>(
                "id" to "service-2",
                "name" to "Beard",
                "price" to 8
            )
        )
    }
}
