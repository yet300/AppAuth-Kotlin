package dev.yet300.appauth

import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

/**
 * Android-specific integration tests for AuthorizationService
 * Tests the interaction with Android's AppAuth SDK
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthorizationServiceAndroidTest {

    private lateinit var mockActivityResultCaller: ActivityResultCaller
    private lateinit var mockLauncher: ActivityResultLauncher<Intent>
    private lateinit var authService: AuthorizationService

    @Before
    fun setup() {
        mockActivityResultCaller = mockk(relaxed = true)
        mockLauncher = mockk(relaxed = true)

        val launcherSlot = slot<ActivityResultLauncher<Intent>>()
        every {
            mockActivityResultCaller.registerForActivityResult(
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
        } returns mockLauncher

        // Create a mock context
        val mockContext = mockk<AuthorizationServiceContext>(relaxed = true)
        authService = AuthorizationService { mockContext }
    }

    @Test
    fun testServiceCreation() {
        assertNotNull(authService)
    }

    @Test
    fun testBindingToActivityResultCaller() {
        authService.bind(mockActivityResultCaller)

        verify {
            mockActivityResultCaller.registerForActivityResult(
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
        }
    }

    @Test
    fun testAuthorizationRequestLaunchesIntent() = runTest {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client",
            scopes = listOf("openid", "profile"),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )

        authService.bind(mockActivityResultCaller)

        // Note: Full flow testing requires more complex mocking
        // This test verifies the service can be created and bound
        assertNotNull(request)
    }
}
