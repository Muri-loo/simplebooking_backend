package pt.simplebooking.repository

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository
import pt.simplebooking.config.Firestore as FirestoreConstants
import pt.simplebooking.mapper.ServiceItemMapper

@Repository
class ServiceCatalogRepository(
    private val firestore: Firestore
) {

    fun removeService(businessId: String, serviceId: String): RemoveServiceResult {
        val catalogReference = firestore
            .collection(FirestoreConstants.Collections.SERVICE_CATALOGS)
            .document(businessId)

        return firestore.runTransaction<RemoveServiceResult> { transaction ->
            val catalog = transaction.get(catalogReference).get()

            if (!catalog.exists()) {
                return@runTransaction RemoveServiceResult.CatalogNotFound
            }

            if (
                catalog.getString(
                    FirestoreConstants.Fields.ServiceCatalog.BUSINESS_ID
                ) != businessId
            ) {
                return@runTransaction RemoveServiceResult.BusinessMismatch
            }

            val services = try {
                ServiceItemMapper.fromRaw(
                    catalog.get(FirestoreConstants.Fields.ServiceCatalog.SERVICES)
                )
            } catch (ex: IllegalArgumentException) {
                return@runTransaction RemoveServiceResult.InvalidCatalog(ex)
            }

            val updatedServices = services.filterNot { it.id == serviceId }

            if (updatedServices.size == services.size) {
                return@runTransaction RemoveServiceResult.ServiceNotFound
            }

            transaction.update(
                catalogReference,
                mapOf(
                    FirestoreConstants.Fields.ServiceCatalog.SERVICES to
                        updatedServices.map(ServiceItemMapper::toMap),
                    FirestoreConstants.Fields.ServiceCatalog.UPDATED_AT to
                        FieldValue.serverTimestamp()
                )
            )

            RemoveServiceResult.Removed
        }.get()
    }
}

sealed interface RemoveServiceResult {
    data object Removed : RemoveServiceResult
    data object CatalogNotFound : RemoveServiceResult
    data object BusinessMismatch : RemoveServiceResult
    data object ServiceNotFound : RemoveServiceResult
    data class InvalidCatalog(val cause: Exception) : RemoveServiceResult
}
