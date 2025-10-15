package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for configuration validation and edge cases
 */
class ConfigurationValidationTest {

    @Test
    fun testMinimalValidConfiguration() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize",
            tokenEndpoint = "https://auth.example.com/token"
        )

        assertEquals("https://auth.example.com/authorize", config.authorizationEndpoint)
        assertEquals("https://auth.example.com/token", config.tokenEndpoint)
        assertNull(config.registrationEndpoint)
        assertNull(config.endSessionEndpoint)
        assertNull(config.revocationEndpoint)
    }

    @Test
    fun testConfigurationWithAllEndpoints() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize",
            tokenEndpoint = "https://auth.example.com/token",
            registrationEndpoint = "https://auth.example.com/register",
            endSessionEndpoint = "https://auth.example.com/logout",
            revocationEndpoint = "https://auth.example.com/revoke"
        )

        assertNotNull(config.authorizationEndpoint)
        assertNotNull(config.tokenEndpoint)
        assertNotNull(config.registrationEndpoint)
        assertNotNull(config.endSessionEndpoint)
        assertNotNull(config.revocationEndpoint)
    }

    @Test
    fun testConfigurationWithTrailingSlashes() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize/",
            tokenEndpoint = "https://auth.example.com/token/"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithQueryParameters() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize?param=value",
            tokenEndpoint = "https://auth.example.com/token?param=value"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithPort() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com:8443/authorize",
            tokenEndpoint = "https://auth.example.com:8443/token"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithSubdomain() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.subdomain.example.com/authorize",
            tokenEndpoint = "https://auth.subdomain.example.com/token"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithPath() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/oauth/v2/authorize",
            tokenEndpoint = "https://example.com/oauth/v2/token"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithDifferentDomains() {
        // Some providers use different domains for different endpoints
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://accounts.example.com/authorize",
            tokenEndpoint = "https://oauth.example.com/token"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationImmutability() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize",
            tokenEndpoint = "https://auth.example.com/token"
        )

        // Verify values don't change
        val endpoint1 = config.authorizationEndpoint
        val endpoint2 = config.authorizationEndpoint
        assertEquals(endpoint1, endpoint2)
    }

    @Test
    fun testConfigurationWithIPAddress() {
        // For local development/testing
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "http://192.168.1.100:8080/authorize",
            tokenEndpoint = "http://192.168.1.100:8080/token"
        )

        assertNotNull(config)
    }

    @Test
    fun testConfigurationWithLocalhost() {
        // For local development
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "http://localhost:8080/authorize",
            tokenEndpoint = "http://localhost:8080/token"
        )

        assertNotNull(config)
    }
}
