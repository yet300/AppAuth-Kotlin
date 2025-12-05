package dev.yet300.appauth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Android-specific test class that runs with Robolectric.
 * This ensures that Android framework classes like Uri are properly mocked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthorizationServiceTestAndroid {
    
    @Test
    fun testFetchFromIssuer() = runTest {
        val originalUrl = "https://oauth-server.com/auth/realms/MyRealm"
        val (mockUrl, cleanup) = setupMockOAuthServer(originalUrl)
        try {
            val actual = AuthorizationServiceConfiguration.fetchFromIssuer(mockUrl)
            assertEquals(
                "$mockUrl/protocol/openid-connect/auth",
                actual.authorizationEndpoint,
            )
        } finally {
            cleanup()
        }
    }
}

