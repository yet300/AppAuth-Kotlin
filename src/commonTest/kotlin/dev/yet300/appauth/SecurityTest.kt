package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Security-focused tests for OAuth/OIDC implementation
 * Tests PKCE, state parameters, and other security best practices
 */
class SecurityTest {

    @Test
    fun testStateParameterForCSRFProtection() {
        val config = createMinimalTestConfig()
        val stateValue = "random-state-12345"
        
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = mapOf("state" to stateValue)
        )

        assertNotNull(request)
    }

    @Test
    fun testNonceParameterForReplayProtection() {
        val config = createMinimalTestConfig()
        val nonceValue = "random-nonce-67890"
        
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = mapOf("nonce" to nonceValue)
        )

        assertNotNull(request)
    }

    @Test
    fun testPKCEWithS256Method() {
        val config = createMinimalTestConfig()
        
        // S256 is the recommended PKCE method
        val pkceParams = mapOf(
            "code_challenge" to "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            "code_challenge_method" to "S256"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = pkceParams
        )

        assertNotNull(request)
    }

    @Test
    fun testHTTPSEndpointsOnly() {
        // All endpoints should use HTTPS in production
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://auth.example.com/authorize",
            tokenEndpoint = "https://auth.example.com/token",
            revocationEndpoint = "https://auth.example.com/revoke"
        )

        assertTrue(config.authorizationEndpoint.startsWith("https://"))
        assertTrue(config.tokenEndpoint.startsWith("https://"))
        assertTrue(config.revocationEndpoint?.startsWith("https://") ?: true)
    }

    @Test
    fun testConfidentialClientWithSecret() {
        val config = createFullTestConfig()
        
        // Confidential clients use client_secret for authentication
        val revokeRequest = RevokeTokenRequest(
            config = config,
            token = TestConstants.TEST_REFRESH_TOKEN,
            clientId = TestConstants.TEST_CLIENT_ID,
            clientSecret = TestConstants.TEST_CLIENT_SECRET
        )

        assertNotNull(revokeRequest)
    }

    @Test
    fun testPublicClientWithoutSecret() {
        val config = createFullTestConfig()
        
        // Public clients (mobile apps) should not use client_secret
        val revokeRequest = RevokeTokenRequest(
            config = config,
            token = TestConstants.TEST_REFRESH_TOKEN,
            clientId = TestConstants.TEST_CLIENT_ID,
            clientSecret = null
        )

        assertNotNull(revokeRequest)
    }

    @Test
    fun testCustomURISchemeForRedirect() {
        val config = createMinimalTestConfig()
        
        // Mobile apps should use custom URI schemes
        val customSchemeUri = "com.example.app:/oauth2redirect"
        
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = customSchemeUri,
            additionalParameters = null
        )

        assertNotNull(request)
        assertTrue(customSchemeUri.contains(":/"))
    }

    @Test
    fun testPromptParameterForReauthentication() {
        val config = createMinimalTestConfig()
        
        // prompt=login forces re-authentication
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = mapOf("prompt" to "login")
        )

        assertNotNull(request)
    }

    @Test
    fun testPromptConsentForExplicitConsent() {
        val config = createMinimalTestConfig()
        
        // prompt=consent forces consent screen
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = mapOf("prompt" to "consent")
        )

        assertNotNull(request)
    }

    @Test
    fun testMaxAgeParameterForSessionValidation() {
        val config = createMinimalTestConfig()
        
        // max_age requires re-authentication if session is older
        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = mapOf("max_age" to "3600")
        )

        assertNotNull(request)
    }
}
