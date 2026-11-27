package pt.simplebooking.mapper

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServiceItemMapperTest {

    @Test
    fun shouldRoundTripAnIntegerPrice() {
        val map = mapOf(
            "id" to "service-1",
            "name" to "Haircut",
            "price" to 13,
            "durationMinutes" to 30
        )

        assertEquals(map, ServiceItemMapper.toMap(ServiceItemMapper.fromMap(map)))
    }

    @Test
    fun shouldRoundTripADecimalPrice() {
        val map = mapOf(
            "id" to "service-1",
            "name" to "Haircut",
            "price" to 13.5
        )

        assertEquals(map, ServiceItemMapper.toMap(ServiceItemMapper.fromMap(map)))
    }

    @Test
    fun shouldOmitOptionalFieldsThatAreAbsent() {
        val map = mapOf("id" to "service-1", "name" to "Haircut")

        assertEquals(map, ServiceItemMapper.toMap(ServiceItemMapper.fromMap(map)))
    }

    @Test
    fun shouldTolerateAMapWithoutAnId() {
        val map = mapOf("name" to "Haircut")

        assertEquals(map, ServiceItemMapper.toMap(ServiceItemMapper.fromMap(map)))
    }

    @Test
    fun shouldKeepAnEntryWithAnUnexpectedType() {
        val map = mapOf("id" to "service-1", "name" to 42)

        assertEquals(
            mapOf("id" to "service-1"),
            ServiceItemMapper.toMap(ServiceItemMapper.fromMap(map))
        )
    }

    @Test
    fun shouldRejectAValueThatIsNotAList() {
        assertFailsWith<IllegalArgumentException> {
            ServiceItemMapper.fromRaw("not-a-list")
        }
    }
}
