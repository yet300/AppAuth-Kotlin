package dev.yet300.appauth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * Tests for AuthorizationServiceConfiguration.
 * 
 * Note: Testing fetchFromIssuer with MockWebServer doesn't work because the AppAuth library
 * uses its own HTTP client (HttpURLConnection) that doesn't connect to MockWebServer.
 * 
 * Instead, we test the configuration construction directly, which is what happens after
 * the discovery document is fetched. This is mocking at a higher level - we're testing
 * the business logic of configuration creation rather than the network layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthorizationServiceConfigurationTestAndroid {

    @Test
    fun testConfigurationConstruction() = runTest {
        // Test that we can create a configuration directly from endpoints
        // This is what happens after fetchFromIssuer parses the discovery document
        val authorizationEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/auth"
        val tokenEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/token"
        val endSessionEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/logout"
        val revocationEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/revoke"

        // Create configuration directly - this is the higher-level approach
        // We're testing the configuration object itself, not the network fetching
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = authorizationEndpoint,
            tokenEndpoint = tokenEndpoint,
            endSessionEndpoint = endSessionEndpoint,
            revocationEndpoint = revocationEndpoint
        )

        // Verify all endpoints are correctly stored
        assertEquals(authorizationEndpoint, config.authorizationEndpoint)
        assertEquals(tokenEndpoint, config.tokenEndpoint)
        assertEquals(endSessionEndpoint, config.endSessionEndpoint)
        assertEquals(revocationEndpoint, config.revocationEndpoint)
    }

    @Test
    fun testConfigurationWithOptionalEndpoints() = runTest {
        // Test configuration with only required endpoints
        val authorizationEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/auth"
        val tokenEndpoint = "https://oauth-server.com/auth/realms/MyRealm/protocol/openid-connect/token"

        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = authorizationEndpoint,
            tokenEndpoint = tokenEndpoint
        )

        assertEquals(authorizationEndpoint, config.authorizationEndpoint)
        assertEquals(tokenEndpoint, config.tokenEndpoint)
        assertEquals(null, config.endSessionEndpoint)
        assertEquals(null, config.revocationEndpoint)
    }
}

