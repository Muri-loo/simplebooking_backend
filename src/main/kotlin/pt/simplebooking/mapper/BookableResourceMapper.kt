package pt.simplebooking.mapper

import com.google.cloud.Timestamp
import pt.simplebooking.config.Firestore
import pt.simplebooking.models.bo.BookableResource
import pt.simplebooking.models.bo.TimeSlot
import pt.simplebooking.models.bo.WeeklySchedule
import java.time.Instant

object BookableResourceMapper {

    fun fromMap(id: String?, map: Map<String, Any>): BookableResource =
        BookableResource(
            id = id,
            uid = map.string(Firestore.Fields.BookableResource.UID),
            name = map.string(Firestore.Fields.BookableResource.NAME),
            emailResource = map.string(
                Firestore.Fields.BookableResource.EMAIL_RESOURCE
            ),
            active = map[Firestore.Fields.BookableResource.ACTIVE] as? Boolean,
            businessId = map.string(
                Firestore.Fields.BookableResource.BUSINESS_ID
            ),
            photoUrl = map.string(Firestore.Fields.BookableResource.PHOTO_URL),
            createdAt = map.instant(Firestore.Fields.BookableResource.CREATED_AT),
            updatedAt = map.instant(Firestore.Fields.BookableResource.UPDATED_AT),
            deletedAt = map.instant(Firestore.Fields.BookableResource.DELETED_AT),
            schedule = WeeklySchedule(
                monday = map.slots(Firestore.Fields.BookableResource.MONDAY),
                tuesday = map.slots(Firestore.Fields.BookableResource.TUESDAY),
                wednesday = map.slots(Firestore.Fields.BookableResource.WEDNESDAY),
                thursday = map.slots(Firestore.Fields.BookableResource.THURSDAY),
                friday = map.slots(Firestore.Fields.BookableResource.FRIDAY),
                saturday = map.slots(Firestore.Fields.BookableResource.SATURDAY),
                sunday = map.slots(Firestore.Fields.BookableResource.SUNDAY)
            )
        )

    private fun Map<String, Any>.string(key: String): String? = this[key] as? String

    private fun Map<String, Any>.instant(key: String): Instant? =
        (this[key] as? Timestamp)?.let {
            Instant.ofEpochSecond(it.seconds, it.nanos.toLong())
        }

    private fun Map<String, Any>.slots(key: String): List<TimeSlot> =
        (this[key] as? List<*>)
            .orEmpty()
            .filterIsInstance<Map<*, *>>()
            .map { slot ->
                TimeSlot(
                    start = slot[Firestore.Fields.BookableResource.SLOT_START]
                        as? String,
                    end = slot[Firestore.Fields.BookableResource.SLOT_END]
                        as? String
                )
            }
}
