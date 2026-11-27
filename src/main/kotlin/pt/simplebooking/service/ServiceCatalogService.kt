
package pt.simplebooking.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
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
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bookable resource not found"
            )

        return resource.businessId
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Business ID not found"
            )
    }

    private fun removeService(businessId: String, serviceId: String) {
        val failure = serviceCatalogRepository
            .removeService(businessId, serviceId)
            .toFailure()

        if (failure != null) {
            throw failure
        }
    }

    private fun RemoveServiceResult.toFailure(): ResponseStatusException? =
        when (this) {
            RemoveServiceResult.Removed -> null
            RemoveServiceResult.CatalogNotFound -> ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Service catalog not found"
            )
            RemoveServiceResult.BusinessMismatch -> ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Service catalog does not belong to this business"
            )
            RemoveServiceResult.ServiceNotFound -> ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Service not found"
            )
            is RemoveServiceResult.InvalidCatalog -> ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invalid service catalog",
                cause
            )
        }
}
