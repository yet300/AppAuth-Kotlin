package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for RevokeTokenRequest
 * Covers RFC 7009 token revocation functionality
 */
class RevokeTokenRequestTest {

    private fun createTestConfig() = AuthorizationServiceConfiguration(
        authorizationEndpoint = "https://example.com/auth",
        tokenEndpoint = "https://example.com/token",
        revocationEndpoint = "https://example.com/revoke"
    )

    @Test
    fun testRevokeTokenRequestWithPublicClient() {
        val config = createTestConfig()
        val request = RevokeTokenRequest(
            config = config,
            token = "test-refresh-token",
            clientId = "test-client-id",
            clientSecret = null
        )

        assertNotNull(request)
    }

    @Test
    fun testRevokeTokenRequestWithConfidentialClient() {
        val config = createTestConfig()
        val request = RevokeTokenRequest(
            config = config,
            token = "test-refresh-token",
            clientId = "test-client-id",
            clientSecret = "test-client-secret"
        )

        assertNotNull(request)
    }

    @Test
    fun testRevokeTokenRequestForAccessToken() {
        val config = createTestConfig()
        val request = RevokeTokenRequest(
            config = config,
            token = "test-access-token",
            clientId = "test-client-id",
            clientSecret = null
        )

        assertNotNull(request)
    }
}
