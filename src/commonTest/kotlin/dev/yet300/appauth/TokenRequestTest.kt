package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for TokenRequest
 * Covers token exchange and refresh token requests
 */
class TokenRequestTest {

    private fun createTestConfig() = AuthorizationServiceConfiguration(
        authorizationEndpoint = "https://example.com/auth",
        tokenEndpoint = "https://example.com/token"
    )

    @Test
    fun testTokenRequestWithAuthorizationCode() {
        val config = createTestConfig()
        val request = TokenRequest(
            config = config,
            clientId = "test-client-id",
            grantType = "authorization_code",
            refreshToken = null
        )

        assertNotNull(request)
    }

    @Test
    fun testTokenRequestWithRefreshToken() {
        val config = createTestConfig()
        val request = TokenRequest(
            config = config,
            clientId = "test-client-id",
            grantType = "refresh_token",
            refreshToken = "test-refresh-token"
        )

        assertNotNull(request)
    }

    @Test
    fun testTokenRequestWithClientCredentials() {
        val config = createTestConfig()
        val request = TokenRequest(
            config = config,
            clientId = "test-client-id",
            grantType = "client_credentials",
            refreshToken = null
        )

        assertNotNull(request)
    }
}
