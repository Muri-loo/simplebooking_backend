
package pt.simplebooking.service

import org.springframework.stereotype.Service
import pt.simplebooking.error.BusinessAccessDeniedException
import pt.simplebooking.error.BusinessResourceNotFoundException
import pt.simplebooking.repository.BookableResourceRepository
import pt.simplebooking.repository.RemoveServiceResult
import pt.simplebooking.repository.ServiceCatalogRepository

@Service
class ServiceCatalogService(
    private val firebaseTokenService: FirebaseTokenService,
    private val bookableResourceRepository: BookableResourceRepository,
    private val serviceCatalogRepository: ServiceCatalogRepository
) {

    fun deleteServiceCatalog(serviceId: String, idToken: String) {
        val decodedToken = firebaseTokenService.verifyToken(idToken)

        firebaseTokenService.verifyAdminRole(decodedToken)

        val businessId = findBusinessId(decodedToken.uid)

        firebaseTokenService.verifyBusinessOwnership(
            decodedToken = decodedToken,
            businessId = businessId
        )
        removeService(businessId, serviceId)
    }

    private fun findBusinessId(uid: String): String {
        val resource = bookableResourceRepository.findByUid(uid)
            ?: throw BusinessResourceNotFoundException(
                "Bookable resource not found"
            )

        return resource.businessId
            ?: throw BusinessAccessDeniedException(
                "Business ID not found"
            )
    }

    private fun removeService(businessId: String, serviceId: String) {
        val failure = serviceCatalogRepository
            .removeService(businessId, serviceId)
            .toException()

        if (failure != null) {
            throw failure
        }
    }

    private fun RemoveServiceResult.toException(): RuntimeException? =
        when (this) {
            RemoveServiceResult.Removed -> null
            RemoveServiceResult.CatalogNotFound ->
                BusinessResourceNotFoundException("Service catalog not found")
            RemoveServiceResult.BusinessMismatch ->
                BusinessAccessDeniedException(
                    "Service catalog does not belong to this business"
                )
            RemoveServiceResult.ServiceNotFound ->
                BusinessResourceNotFoundException("Service not found")
            is RemoveServiceResult.InvalidCatalog ->
                IllegalStateException("Invalid service catalog", cause)
        }
}
