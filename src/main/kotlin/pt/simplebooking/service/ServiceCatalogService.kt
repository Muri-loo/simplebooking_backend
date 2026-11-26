
package pt.simplebooking.service

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore as FirestoreClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.simplebooking.config.Firestore

@Service
class ServiceCatalogService(
    private val firebaseTokenService: FirebaseTokenService,
    private val firestore: FirestoreClient
) {

    fun deleteServiceCatalog(serviceId: String, idToken: String) {
        val decodedToken = firebaseTokenService.verifyToken(idToken)

        firebaseTokenService.verifyAdminRole(decodedToken)

        val businessId = findBusinessId(decodedToken.uid)

        firebaseTokenService.verifyBusinessOwnership(
            decodedToken = decodedToken,
            businessId = businessId
        )

        deleteServiceFromCatalog(businessId, serviceId)
    }

    private fun findBusinessId(uid: String): String {
        val bookableResource = firestore
            .collection(Firestore.Collections.BOOKABLE_RESOURCES)
            .whereEqualTo(Firestore.Fields.BookableResource.UID, uid)
            .limit(1)
            .get()
            .get()
            .documents
            .firstOrNull()
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bookable resource not found"
            )

        return bookableResource.getString(
            Firestore.Fields.BookableResource.BUSINESS_ID
        ) ?: throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Business ID not found"
        )
    }

    private fun deleteServiceFromCatalog(
        businessId: String,
        serviceId: String
    ) {
        firestore.runTransaction { transaction ->
            val catalogRef = firestore
                .collection(Firestore.Collections.SERVICE_CATALOGS)
                .document(businessId)

            val catalog = transaction.get(catalogRef).get()

            validateCatalogOwnership(
                catalog = catalog,
                businessId = businessId
            )

            val services = getServices(catalog)
            val updatedServices = removeService(services, serviceId)

            transaction.update(
                catalogRef,
                mapOf(
                    Firestore.Fields.ServiceCatalog.SERVICES to updatedServices,
                    Firestore.Fields.ServiceCatalog.UPDATED_AT to FieldValue.serverTimestamp()
                )
            )
        }.get()
    }

    private fun validateCatalogOwnership(
        catalog: com.google.cloud.firestore.DocumentSnapshot,
        businessId: String
    ) {
        if (!catalog.exists()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Service catalog not found"
            )
        }

        if (
            catalog.getString(
                Firestore.Fields.ServiceCatalog.BUSINESS_ID
            ) != businessId
        ) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Service catalog does not belong to this business"
            )
        }
    }

    private fun getServices(
        catalog: com.google.cloud.firestore.DocumentSnapshot
    ): List<Map<String, Any>> {
        @Suppress("UNCHECKED_CAST")
        return catalog.get(
            Firestore.Fields.ServiceCatalog.SERVICES
        ) as? List<Map<String, Any>>
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invalid service catalog"
            )
    }

    private fun removeService(
        services: List<Map<String, Any>>,
        serviceId: String
    ): List<Map<String, Any>> {
        val updatedServices = services.filterNot {
            it[Firestore.Fields.Service.ID] == serviceId
        }

        if (updatedServices.size == services.size) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Service not found"
            )
        }

        return updatedServices
    }
}
