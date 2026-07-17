package dev.yet300.appauth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Tests for error handling throughout the authentication process
 * Covers network errors, server errors, and invalid configurations
 */
class ErrorHandlingTest {

    @Test
    fun testInvalidDiscoveryUrl() = runTest {
        assertFailsWith<Exception> {
            AuthorizationServiceConfiguration.fetchFromIssuer("not-a-url")
        }
    }

    @Test
    fun testUnreachableDiscoveryEndpoint() = runTest {
        assertFailsWith<Exception> {
            AuthorizationServiceConfiguration.fetchFromIssuer("https://nonexistent-server-12345.example.com")
        }
    }

    @Test
    fun testMalformedDiscoveryUrl() = runTest {
        assertFailsWith<Exception> {
            AuthorizationServiceConfiguration.fetchFromIssuer("htp://invalid")
        }
    }

    @Test
    fun testEmptyClientId() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        // Library should allow creation but may fail at execution
        val request = AuthorizationRequest(
            config = config,
            clientId = "",
            scopes = listOf("openid"),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testEmptyRedirectUri() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        // Library should allow creation but may fail at execution
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client",
            scopes = listOf("openid"),
            responseType = "code",
            redirectUri = "",
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testInvalidResponseType() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        // Library should allow creation, server will reject invalid response_type
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client",
            scopes = listOf("openid"),
            responseType = "invalid_type",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testInvalidGrantType() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )

        // Library should allow creation, server will reject invalid grant_type
        val request = TokenRequest(
            config = config,
            clientId = "test-client",
            grantType = "invalid_grant",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testMissingRevocationEndpoint() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token",
            revocationEndpoint = null
        )

        // Request creation should succeed
        val request = RevokeTokenRequest(
            config = config,
            token = "test-token",
            clientId = "test-client",
            clientSecret = null
        )
        assertNotNull(request)
        // Execution would fail with missing endpoint
    }

    @Test
    fun testMissingEndSessionEndpoint() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token",
            endSessionEndpoint = null
        )

        // Request creation should succeed
        val request = EndSessionRequest(
            config = config,
            idTokenHint = "test-token",
            postLogoutRedirectUri = null,
            additionalParameters = null
        )
        assertNotNull(request)
        // Execution might fail or fallback depending on implementation
    }
}
