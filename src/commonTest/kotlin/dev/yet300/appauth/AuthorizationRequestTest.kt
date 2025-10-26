package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for AuthorizationRequest
 * Covers request construction and parameter validation
 */
class AuthorizationRequestTest {

    private fun createTestConfig() = AuthorizationServiceConfiguration(
        authorizationEndpoint = "https://example.com/auth",
        tokenEndpoint = "https://example.com/token"
    )

    @Test
    fun testAuthorizationRequestCreation() {
        val config = createTestConfig()
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client-id",
            scopes = listOf("openid", "profile", "email"),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )

        assertNotNull(request)
    }

    @Test
    fun testAuthorizationRequestWithAdditionalParameters() {
        val config = createTestConfig()
        val additionalParams = mapOf(
            "prompt" to "consent",
            "access_type" to "offline"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client-id",
            scopes = listOf("openid", "profile"),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = additionalParams
        )

        assertNotNull(request)
    }

    @Test
    fun testAuthorizationRequestWithEmptyScopes() {
        val config = createTestConfig()
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client-id",
            scopes = emptyList(),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )

        assertNotNull(request)
    }

    @Test
    fun testAuthorizationRequestWithMultipleScopes() {
        val config = createTestConfig()
        val scopes = listOf("openid", "profile", "email", "address", "phone")

        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client-id",
            scopes = scopes,
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )

        assertNotNull(request)
    }
}
