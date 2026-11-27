package pt.simplebooking.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import pt.simplebooking.service.FirebaseTokenService
import pt.simplebooking.service.ServiceCatalogService

@RestController
class ServiceCatalogController(
    private val serviceCatalogService: ServiceCatalogService,
    private val firebaseTokenService: FirebaseTokenService
) {
    @DeleteMapping("/service-catalog/{id}")
    fun deleteServiceCatalog(
        @PathVariable("id") id: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Void> {
        val idToken = firebaseTokenService.extractBearerToken(authHeader)
        serviceCatalogService.deleteServiceCatalog(id, idToken)
        return ResponseEntity.noContent().build()
    }
}
