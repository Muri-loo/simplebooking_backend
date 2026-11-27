package pt.simplebooking.mapper

import pt.simplebooking.config.Firestore
import pt.simplebooking.models.bo.ServiceItem

object ServiceItemMapper {

    fun fromRaw(value: Any?): List<ServiceItem> {
        @Suppress("UNCHECKED_CAST")
        val items = value as? List<Map<String, Any>>
            ?: throw IllegalArgumentException("Service catalog is not a list")

        return items.map(::fromMap)
    }

    fun fromMap(map: Map<String, Any>): ServiceItem = ServiceItem(
        id = map[Firestore.Fields.Service.ID] as? String,
        name = map[Firestore.Fields.Service.NAME] as? String,
        color = map[Firestore.Fields.Service.COLOR] as? String,
        durationMinutes = (map[Firestore.Fields.Service.DURATION_MINUTES] as? Number)?.toInt(),
        price = map[Firestore.Fields.Service.PRICE] as? Number
    )

    fun toMap(item: ServiceItem): Map<String, Any> = buildMap {
        item.id?.let { put(Firestore.Fields.Service.ID, it) }
        item.name?.let { put(Firestore.Fields.Service.NAME, it) }
        item.color?.let { put(Firestore.Fields.Service.COLOR, it) }
        item.durationMinutes?.let { put(Firestore.Fields.Service.DURATION_MINUTES, it) }
        item.price?.let { put(Firestore.Fields.Service.PRICE, it) }
    }
}
