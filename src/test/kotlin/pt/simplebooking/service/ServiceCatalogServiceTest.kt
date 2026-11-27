package pt.simplebooking.service

import com.google.firebase.auth.FirebaseToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import pt.simplebooking.error.AuthenticationFailedException
import pt.simplebooking.error.BusinessAccessDeniedException
import pt.simplebooking.error.BusinessException
import pt.simplebooking.error.BusinessResourceNotFoundException
import pt.simplebooking.models.bo.BookableResource
import pt.simplebooking.repository.BookableResourceRepository
import pt.simplebooking.repository.RemoveServiceResult
import pt.simplebooking.repository.ServiceCatalogRepository
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(OutputCaptureExtension::class)
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
    fun shouldLogOnlyStartAndOutcomeAtInfo(output: CapturedOutput) {
        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, FLOW_SERVICE_ID)
        } returns RemoveServiceResult.Removed

        val capturedBefore = output.all.length
        service.deleteServiceCatalog(serviceId = FLOW_SERVICE_ID, idToken = VALID_TOKEN)
        val flow = output.all.substring(capturedBefore)

        val started = flow.indexOf("Starting service deletion: serviceId=$FLOW_SERVICE_ID")
        val finished = flow.indexOf(
            "Service deleted: businessId=$BUSINESS_ID, serviceId=$FLOW_SERVICE_ID"
        )

        assertTrue(started >= 0, "Missing start log line")
        assertTrue(finished > started, "Missing or out-of-order completion log line")
        assertTrue(
            flow.lines().none { it.contains("Deleting service from Firestore") },
            "Debug detail must not be logged at INFO level"
        )
    }

    @Test
    fun shouldRejectDeletionWhenTokenIsInvalid() {
        // Arrange
        every {
            mockFirebaseTokenService.verifyToken(INVALID_TOKEN)
        } throws AuthenticationFailedException("Invalid Firebase ID token")

        // Act
        val ex = assertFailsWith<AuthenticationFailedException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = INVALID_TOKEN
            )
        }

        // Assert
        assertEquals("Invalid Firebase ID token", ex.message)
        verify(exactly = 0) {
            mockServiceCatalogRepository.removeService(any(), any())
        }
    }

    @Test
    fun shouldRejectDeletionWhenAdminClaimIsMissingOrFalse() {
        // Arrange
        every {
            mockFirebaseTokenService.verifyAdminRole(mockDecodedToken)
        } throws BusinessAccessDeniedException(
            "User does not have admin privileges"
        )

        // Act
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        // Assert
        assertEquals("User does not have admin privileges", ex.message)
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
        } throws BusinessAccessDeniedException(
            "User does not own this business"
        )

        // Act
        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        // Assert
        assertEquals("User does not own this business", ex.message)
        verify(exactly = 0) {
            mockServiceCatalogRepository.removeService(any(), any())
        }
    }

    @Test
    fun shouldReturnNotFoundWhenBookableResourceIsMissing() {
        every {
            mockBookableResourceRepository.findByUid(USER_ID)
        } returns null

        val ex = assertFailsWith<BusinessResourceNotFoundException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals("Bookable resource not found", ex.message)
    }

    @Test
    fun shouldReturnForbiddenWhenBusinessIdIsMissing() {
        every {
            mockBookableResourceRepository.findByUid(USER_ID)
        } returns BookableResource(businessId = null)

        val ex = assertFailsWith<BusinessAccessDeniedException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals("Business ID not found", ex.message)
    }

    @Test
    fun shouldReturnNotFoundWhenCatalogIsMissing() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.CatalogNotFound,
            expectedType = BusinessResourceNotFoundException::class,
            expectedReason = "Service catalog not found"
        )
    }

    @Test
    fun shouldReturnForbiddenWhenCatalogBelongsToAnotherBusiness() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.BusinessMismatch,
            expectedType = BusinessAccessDeniedException::class,
            expectedReason = "Service catalog does not belong to this business"
        )
    }

    @Test
    fun shouldReturnNotFoundWhenServiceIsMissing() {
        assertRemoveServiceFailure(
            result = RemoveServiceResult.ServiceNotFound,
            expectedType = BusinessResourceNotFoundException::class,
            expectedReason = "Service not found"
        )
    }

    @Test
    fun shouldReturnInternalServerErrorWhenCatalogIsInvalid() {
        val cause = IllegalArgumentException("Service catalog is not a list")

        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        } returns RemoveServiceResult.InvalidCatalog(cause)

        val ex = assertFailsWith<IllegalStateException> {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals("Invalid service catalog", ex.message)
        assertEquals(cause, ex.cause)
    }

    private fun assertRemoveServiceFailure(
        result: RemoveServiceResult,
        expectedType: KClass<out BusinessException>,
        expectedReason: String
    ) {
        every {
            mockServiceCatalogRepository.removeService(BUSINESS_ID, SERVICE_ID)
        } returns result

        val ex = assertFailsWith(expectedType) {
            service.deleteServiceCatalog(
                serviceId = SERVICE_ID,
                idToken = VALID_TOKEN
            )
        }

        assertEquals(expectedReason, ex.message)
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
        const val FLOW_SERVICE_ID = "service-flow-1"
        const val VALID_TOKEN = "valid-token"
        const val INVALID_TOKEN = "invalid-token"
    }
}
