package dev.yet300.appauth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Android-specific wrapper for the common AuthorizationServiceTest.
 * This ensures the test runs with Robolectric, which is required for Android framework classes.
 * 
 * This duplicates the test from CommonTest.kt but with @RunWith annotation for Android.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthorizationServiceTestAndroidWrapper {
    
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

