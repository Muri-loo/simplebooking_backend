package pt.simplebooking.mapper

import com.google.cloud.Timestamp
import org.junit.jupiter.api.Test
import pt.simplebooking.config.Firestore
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookableResourceMapperTest {

    @Test
    fun shouldPreserveSubMillisecondPrecisionWhenMappingTimestamps() {
        val seconds = 1_700_000_000L
        val nanos = 123_456_789

        val resource = BookableResourceMapper.fromMap(
            id = "resource-1",
            map = mapOf(
                Firestore.Fields.BookableResource.CREATED_AT to
                    Timestamp.ofTimeSecondsAndNanos(seconds, nanos)
            )
        )

        assertEquals(
            Instant.ofEpochSecond(seconds, nanos.toLong()),
            resource.createdAt
        )
    }

    @Test
    fun shouldMapMissingTimestampsToNull() {
        val resource = BookableResourceMapper.fromMap(
            id = "resource-1",
            map = emptyMap()
        )

        assertNull(resource.createdAt)
        assertNull(resource.updatedAt)
        assertNull(resource.deletedAt)
    }
}
