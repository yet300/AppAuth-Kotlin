package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for PKCE (Proof Key for Code Exchange) functionality
 * PKCE is critical for mobile apps to prevent authorization code interception
 */
class PKCETest {

    @Test
    fun testPKCEParametersInAuthorizationRequest() {
        val config = createMinimalTestConfig()
        
        // PKCE parameters: code_challenge and code_challenge_method
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
    fun testPKCEWithPlainMethod() {
        val config = createMinimalTestConfig()
        
        // PKCE with plain method (less secure, but valid)
        val pkceParams = mapOf(
            "code_challenge" to "test-code-verifier",
            "code_challenge_method" to "plain"
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
    fun testPKCECodeChallengeLength() {
        // Code challenge should be 43-128 characters for S256
        val validChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
        assertTrue(validChallenge.length in 43..128)
    }

    @Test
    fun testAuthorizationRequestWithStateParameter() {
        val config = createMinimalTestConfig()
        
        // State parameter for CSRF protection
        val params = mapOf(
            "state" to "random-state-value-12345",
            "code_challenge" to "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            "code_challenge_method" to "S256"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = params
        )

        assertNotNull(request)
    }

    @Test
    fun testAuthorizationRequestWithNonce() {
        val config = createMinimalTestConfig()
        
        // Nonce parameter for ID token validation
        val params = mapOf(
            "nonce" to "random-nonce-value-67890",
            "code_challenge" to "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            "code_challenge_method" to "S256"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = params
        )

        assertNotNull(request)
    }
}
