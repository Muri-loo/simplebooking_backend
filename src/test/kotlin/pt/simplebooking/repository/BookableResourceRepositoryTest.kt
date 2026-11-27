package pt.simplebooking.repository

import com.google.api.core.ApiFuture
import com.google.cloud.Timestamp
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.simplebooking.config.Firestore as FirestoreConstants
import pt.simplebooking.models.bo.BookableResource
import pt.simplebooking.models.bo.TimeSlot
import pt.simplebooking.models.bo.WeeklySchedule
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookableResourceRepositoryTest {

    private lateinit var repository: BookableResourceRepository

    private val mockFirestore = mockk<Firestore>()
    private val resourceCollection = mockk<CollectionReference>()
    private val resourceQuery = mockk<Query>()
    private val resourceFuture = mockk<ApiFuture<QuerySnapshot>>()
    private val resourceSnapshot = mockk<QuerySnapshot>()
    private val resourceDocument = mockk<QueryDocumentSnapshot>()

    @BeforeEach
    fun setup() {
        repository = BookableResourceRepository(mockFirestore)

        every {
            mockFirestore.collection(FirestoreConstants.Collections.BOOKABLE_RESOURCES)
        } returns resourceCollection

        every {
            resourceCollection.whereEqualTo(
                FirestoreConstants.Fields.BookableResource.UID,
                USER_ID
            )
        } returns resourceQuery

        every { resourceQuery.limit(1) } returns resourceQuery
        every { resourceQuery.get() } returns resourceFuture
        every { resourceFuture.get() } returns resourceSnapshot
    }

    @Test
    fun shouldMapTheStoredDocumentIntoABookableResource() {
        // Arrange
        every { resourceSnapshot.documents } returns listOf(resourceDocument)
        every { resourceDocument.id } returns RESOURCE_ID
        every { resourceDocument.data } returns storedDocument

        // Act
        val resource = repository.findByUid(USER_ID)

        // Assert
        assertEquals(expectedResource, resource)
    }

    @Test
    fun shouldReturnNullWhenNoResourceMatches() {
        every { resourceSnapshot.documents } returns emptyList()

        assertNull(repository.findByUid(USER_ID))
    }

    private companion object {
        const val BUSINESS_ID = "9rUlQpKhQaPy4zOLTlir"
        const val USER_ID = "0GkfW3VcZiOkkTjBT40RTpTDCDk1"
        const val RESOURCE_ID = "resource-doc-1"
        const val CREATED_AT_MILLIS = 1_763_309_087_000L
        const val UPDATED_AT_MILLIS = 1_763_391_419_000L

        val storedDocument = mapOf<String, Any>(
            "uid" to USER_ID,
            "name" to "Rodrigo Camões Sintra",
            "emailResource" to "rodrigocamoessintra2@gmail.com",
            "active" to true,
            "businessId" to BUSINESS_ID,
            "photoUrl" to "gs://bucket/businesses/$BUSINESS_ID/photo.jpg",
            "createdAt" to Timestamp.ofTimeSecondsAndNanos(
                CREATED_AT_MILLIS / 1000,
                0
            ),
            "updatedAt" to Timestamp.ofTimeSecondsAndNanos(
                UPDATED_AT_MILLIS / 1000,
                0
            ),
            "monday" to listOf(mapOf("start" to "09:00", "end" to "20:00")),
            "tuesday" to emptyList<Any>(),
            "wednesday" to emptyList<Any>(),
            "thursday" to emptyList<Any>(),
            "friday" to emptyList<Any>(),
            "saturday" to emptyList<Any>(),
            "sunday" to emptyList<Any>()
        )

        val expectedResource = BookableResource(
            id = RESOURCE_ID,
            uid = USER_ID,
            name = "Rodrigo Camões Sintra",
            emailResource = "rodrigocamoessintra2@gmail.com",
            active = true,
            businessId = BUSINESS_ID,
            photoUrl = "gs://bucket/businesses/$BUSINESS_ID/photo.jpg",
            createdAt = Instant.ofEpochMilli(CREATED_AT_MILLIS),
            updatedAt = Instant.ofEpochMilli(UPDATED_AT_MILLIS),
            deletedAt = null,
            schedule = WeeklySchedule(
                monday = listOf(TimeSlot(start = "09:00", end = "20:00"))
            )
        )
    }
}
