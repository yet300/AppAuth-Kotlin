package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for different OAuth 2.0 grant types
 * Covers authorization_code, refresh_token, and client_credentials
 */
class GrantTypeTest {

    @Test
    fun testAuthorizationCodeGrant() {
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "authorization_code",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testRefreshTokenGrant() {
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "refresh_token",
            refreshToken = TestConstants.TEST_REFRESH_TOKEN
        )
        assertNotNull(request)
    }

    @Test
    fun testClientCredentialsGrant() {
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "client_credentials",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testPasswordGrant() {
        // Password grant (not recommended for mobile apps)
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "password",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testImplicitGrant() {
        // Implicit grant via response_type=token (deprecated)
        val request = AuthorizationRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "token",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testHybridFlow() {
        // Hybrid flow with response_type=code id_token
        val request = AuthorizationRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code id_token",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testCodeIdTokenTokenResponseType() {
        // Hybrid flow with all response types
        val request = AuthorizationRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            scopes = TestConstants.TEST_SCOPES,
            responseType = "code id_token token",
            redirectUri = TestConstants.TEST_REDIRECT_URI,
            additionalParameters = null
        )
        assertNotNull(request)
    }

    @Test
    fun testDeviceCodeGrant() {
        // Device code grant (RFC 8628)
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "urn:ietf:params:oauth:grant-type:device_code",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testJWTBearerGrant() {
        // JWT Bearer grant (RFC 7523)
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer",
            refreshToken = null
        )
        assertNotNull(request)
    }

    @Test
    fun testSAMLBearerGrant() {
        // SAML Bearer grant (RFC 7522)
        val request = TokenRequest(
            config = createMinimalTestConfig(),
            clientId = TestConstants.TEST_CLIENT_ID,
            grantType = "urn:ietf:params:oauth:grant-type:saml2-bearer",
            refreshToken = null
        )
        assertNotNull(request)
    }
}
