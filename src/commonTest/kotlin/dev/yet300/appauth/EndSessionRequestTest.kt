package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for EndSessionRequest
 * Covers OIDC RP-Initiated Logout functionality
 */
class EndSessionRequestTest {

    private fun createTestConfig() = AuthorizationServiceConfiguration(
        authorizationEndpoint = "https://example.com/auth",
        tokenEndpoint = "https://example.com/token",
        endSessionEndpoint = "https://example.com/logout"
    )

    @Test
    fun testEndSessionRequestWithIdToken() {
        val config = createTestConfig()
        val request = EndSessionRequest(
            config = config,
            idTokenHint = "test-id-token",
            postLogoutRedirectUri = "com.example.app:/logout",
            additionalParameters = null
        )

        assertNotNull(request)
    }

    @Test
    fun testEndSessionRequestWithoutIdToken() {
        val config = createTestConfig()
        val request = EndSessionRequest(
            config = config,
            idTokenHint = null,
            postLogoutRedirectUri = "com.example.app:/logout",
            additionalParameters = null
        )

        assertNotNull(request)
    }

    @Test
    fun testEndSessionRequestWithAdditionalParameters() {
        val config = createTestConfig()
        val additionalParams = mapOf("ui_locales" to "en-US")

        val request = EndSessionRequest(
            config = config,
            idTokenHint = "test-id-token",
            postLogoutRedirectUri = "com.example.app:/logout",
            additionalParameters = additionalParams
        )

        assertNotNull(request)
    }

    @Test
    fun testEndSessionRequestMinimal() {
        val config = createTestConfig()
        val request = EndSessionRequest(
            config = config,
            idTokenHint = null,
            postLogoutRedirectUri = null,
            additionalParameters = null
        )

        assertNotNull(request)
    }
}
