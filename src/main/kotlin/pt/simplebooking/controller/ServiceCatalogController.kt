package pt.simplebooking.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import pt.simplebooking.service.ServiceCatalogService

@RestController
class ServiceCatalogController(
    private val serviceCatalogService: ServiceCatalogService
) {
    @DeleteMapping("/service-catalog/{id}")
    fun deleteServiceCatalog(
        @PathVariable("id") id: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Void> {
        if (!authHeader.startsWith("Bearer ", ignoreCase = true)) {
            return ResponseEntity.badRequest().build()
        }
        val idToken = authHeader.substring(7).trim()
        serviceCatalogService.deleteServiceCatalog(id, idToken)
        return ResponseEntity.noContent().build()
    }
}
