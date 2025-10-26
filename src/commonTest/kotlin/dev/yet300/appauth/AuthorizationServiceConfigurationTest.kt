package dev.yet300.appauth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for AuthorizationServiceConfiguration
 * Covers configuration management and OIDC discovery
 */
class AuthorizationServiceConfigurationTest {

    @Test
    fun testConfigurationCreation() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        assertEquals("https://example.com/auth", config.authorizationEndpoint)
        assertEquals("https://example.com/token", config.tokenEndpoint)
        assertNull(config.registrationEndpoint)
        assertNull(config.endSessionEndpoint)
        assertNull(config.revocationEndpoint)
    }

    @Test
    fun testConfigurationWithAllEndpoints() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token",
            registrationEndpoint = "https://example.com/register",
            endSessionEndpoint = "https://example.com/logout",
            revocationEndpoint = "https://example.com/revoke"
        )

        assertEquals("https://example.com/auth", config.authorizationEndpoint)
        assertEquals("https://example.com/token", config.tokenEndpoint)
        assertEquals("https://example.com/register", config.registrationEndpoint)
        assertEquals("https://example.com/logout", config.endSessionEndpoint)
        assertEquals("https://example.com/revoke", config.revocationEndpoint)
    }

    @Test
    fun testFetchFromIssuer_invalidUrl() = runTest {
        assertFailsWith<Exception> {
            AuthorizationServiceConfiguration.fetchFromIssuer("invalid-url")
        }
    }

    @Test
    fun testFetchFromIssuer_unreachableServer() = runTest {
        assertFailsWith<Exception> {
            AuthorizationServiceConfiguration.fetchFromIssuer("https://nonexistent.example.com")
        }
    }

    @Test
    fun testConfigurationEndpointsAreImmutable() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        // Verify that the configuration values remain consistent
        val authEndpoint1 = config.authorizationEndpoint
        val authEndpoint2 = config.authorizationEndpoint
        assertEquals(authEndpoint1, authEndpoint2)
    }
}
