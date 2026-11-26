package pt.simplebooking.controller

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import pt.simplebooking.service.ServiceCatalogService

class ServiceCatalogControllerTest {

    private lateinit var controller: ServiceCatalogController
    private lateinit var mockMvc: MockMvc

    private val mockServiceCatalogService = mockk<ServiceCatalogService>()

    @BeforeEach
    fun setup() {
        controller = ServiceCatalogController(
            serviceCatalogService = mockServiceCatalogService
        )

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .build()
    }

    @Test
    fun shouldReturnNoContentWhenServiceIsDeleted() {
        // Arrange
        val serviceId = "service-123"
        val idToken = "valid-token"

        every {
            mockServiceCatalogService.deleteServiceCatalog(serviceId, idToken)
        } just runs

        // Act & Assert
        mockMvc.perform(
            delete("/service-catalog/$serviceId")
                .header("Authorization", "Bearer $idToken")
        )
            .andExpect(status().isNoContent())

        verify(exactly = 1) {
            mockServiceCatalogService.deleteServiceCatalog(serviceId, idToken)
        }
    }

    @Test
    fun shouldReturnBadRequestWhenAuthorizationHeaderIsNotBearer() {
        // Arrange
        val serviceId = "service-123"

        // Act & Assert
        mockMvc.perform(
            delete("/service-catalog/$serviceId")
                .header("Authorization", "Invalid token")
        )
            .andExpect(status().isBadRequest())

        verify(exactly = 0) {
            mockServiceCatalogService.deleteServiceCatalog(any(), any())
        }
    }
}
