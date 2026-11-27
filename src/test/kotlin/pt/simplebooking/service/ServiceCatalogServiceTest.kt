
package pt.simplebooking.service

import com.google.firebase.auth.FirebaseToken
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import pt.simplebooking.models.bo.BookableResource
import pt.simplebooking.repository.BookableResourceRepository
import pt.simplebooking.repository.RemoveServiceResult
import pt.simplebooking.repository.ServiceCatalogRepository
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServiceCatalogServiceTest {

    private lateinit var service: ServiceCatalogService

    private val mockFirebaseTokenService = mockk<FirebaseTokenService>(relaxed = true)
    private val mockBookableResourceRepository = mockk<BookableResourceRepository>()
    private val mockServiceCatalogRepository = mockk<ServiceCatalogRepository>()
    private val mockDecodedToken = mockk<FirebaseToken>()

    @BeforeEach
    fun setup() {
        service = ServiceCatalogService(
            firebaseTokenService = mockFirebaseTokenService,
            bookableResourceRepository = mockBookableResourceRepository,
            serviceCatalogRepository = mockServiceCatalogRepository
        )

        mockAuthentication()

        every {
            mockBookableResourceRepository.findByUid(USER_ID)
        } returns BookableResource(businessId = BUSINESS_ID)

        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        } returns RemoveServiceResult.Removed
    }

    @Test
    fun shouldRemoveRequestedService() {
        // Act
        service.deleteServiceCatalog(
            serviceId = SERVICE_ID,
            idToken = VALID_TOKEN
        )

        // Assert
        verify(exactly = 1) {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        }

        verify(exactly = 1) {
            mockFirebaseTokenService.verifyBusinessOwnership(
                mockDecodedToken,
                BUSINESS_ID
            )
        }
    }

    @Test
    fun shouldRejectDeletionWhenTokenIsInvalid() {
        // Arrange
        every {
            mockFirebaseTokenService.verifyToken(INVALID_TOKEN)
        } throws ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid Firebase ID token"
        )

        // Act
        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = INVALID_TOKEN
            )
        }

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        verify(exactly = 0) {
            mockServiceCatalogRepository.removeService(any(), any())
        }
    }

    @Test
    fun shouldRejectDeletionWhenAdminClaimIsMissingOrFalse() {
        // Arrange
        every {
            mockFirebaseTokenService.verifyAdminRole(mockDecodedToken)
        } throws ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User does not have admin privileges"
        )

        // Act
        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        verify(exactly = 0) {
            mockFirebaseTokenService.verifyBusinessOwnership(any(), any())
        }
        verify(exactly = 0) {
            mockServiceCatalogRepository.removeService(any(), any())
        }
    }

    @Test
    fun shouldRejectDeletionWhenBusinessOwnershipDoesNotMatch() {
        // Arrange
        every {
            mockFirebaseTokenService.verifyBusinessOwnership(
                mockDecodedToken,
                BUSINESS_ID
            )
        } throws ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User does not own this business"
        )

        // Act
        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        verify(exactly = 0) {
            mockServiceCatalogRepository.removeService(any(), any())
        }
    }

    @Test
    fun shouldReturnNotFoundWhenBookableResourceIsMissing() {
        every {
            mockBookableResourceRepository.findByUid(USER_ID)
        } returns null

        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        assertEquals("Bookable resource not found", ex.reason)
    }

    @Test
    fun shouldReturnForbiddenWhenBusinessIdIsMissing() {
        every {
            mockBookableResourceRepository.findByUid(USER_ID)
        } returns BookableResource(businessId = null)

        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("Business ID not found", ex.reason)
    }

    @Test
    fun shouldReturnNotFoundWhenCatalogIsMissing() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.CatalogNotFound,
            expectedStatus = HttpStatus.NOT_FOUND,
            expectedReason = "Service catalog not found"
        )
    }

    @Test
    fun shouldReturnForbiddenWhenCatalogBelongsToAnotherBusiness() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.BusinessMismatch,
            expectedStatus = HttpStatus.FORBIDDEN,
            expectedReason = "Service catalog does not belong to this business"
        )
    }

    @Test
    fun shouldReturnNotFoundWhenServiceIsMissing() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.ServiceNotFound,
            expectedStatus = HttpStatus.NOT_FOUND,
            expectedReason = "Service not found"
        )
    }

    @Test
    fun shouldReturnInternalServerErrorWhenCatalogIsInvalid() {
        val cause = IllegalArgumentException("Service catalog is not a list")

        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        } returns RemoveServiceResult.InvalidCatalog(cause)

        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.statusCode)
        assertEquals("Invalid service catalog", ex.reason)
        assertEquals(cause, ex.cause)
    }

    private fun assertRemoveServiceFailure(
        result: RemoveServiceResult,
        expectedStatus: HttpStatus,
        expectedReason: String
    ) {
        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        } returns result

        val ex = assertFailsWith<ResponseStatusException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals(expectedStatus, ex.statusCode)
        assertEquals(expectedReason, ex.reason)
    }

    private fun mockAuthentication() {
        every {
            mockFirebaseTokenService.verifyToken(VALID_TOKEN)
        } returns mockDecodedToken

        every { mockDecodedToken.uid } returns USER_ID

        every {
            mockFirebaseTokenService.verifyAdminRole(mockDecodedToken)
        } just runs

        every {
            mockFirebaseTokenService.verifyBusinessOwnership(
                mockDecodedToken,
                BUSINESS_ID
            )
        } just runs
    }

    private companion object {
        const val BUSINESS_ID = "business-123"
        const val USER_ID = "user-123"
        const val SERVICE_ID = "service-1"
        const val VALID_TOKEN = "valid-token"
        const val INVALID_TOKEN = "invalid-token"
    }
}
