package pt.simplebooking.models.bo

import java.time.Instant

data class BookableResource(
    val id: String? = null,
    val uid: String? = null,
    val name: String? = null,
    val emailResource: String? = null,
    val active: Boolean? = null,
    val businessId: String? = null,
    val photoUrl: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val deletedAt: Instant? = null,
    val schedule: WeeklySchedule = WeeklySchedule()
)

data class WeeklySchedule(
    val monday: List<TimeSlot> = emptyList(),
    val tuesday: List<TimeSlot> = emptyList(),
    val wednesday: List<TimeSlot> = emptyList(),
    val thursday: List<TimeSlot> = emptyList(),
    val friday: List<TimeSlot> = emptyList(),
    val saturday: List<TimeSlot> = emptyList(),
    val sunday: List<TimeSlot> = emptyList()
)

data class TimeSlot(
    val start: String? = null,
    val end: String? = null
)
