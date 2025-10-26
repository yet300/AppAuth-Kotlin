package dev.yet300.appauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Wrapper tests for Android implementation
 * These test YOUR wrapper code, not the AppAuth library
 */
@RunWith(AndroidJUnit4::class)
class AuthorizationServiceConfigurationWrapperTest {

    @Test
    fun testConfigurationWrapperMapsEndpointsCorrectly() {
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
        assertNotNull(config.android)
    }

    @Test
    fun testConfigurationWrapperHandlesNullEndpoints() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token",
            registrationEndpoint = null,
            endSessionEndpoint = null,
            revocationEndpoint = null
        )

        assertNotNull(config.authorizationEndpoint)
        assertNotNull(config.tokenEndpoint)
        assertNull(config.registrationEndpoint)
        assertNull(config.endSessionEndpoint)
        assertNull(config.revocationEndpoint)
    }
}

@RunWith(AndroidJUnit4::class)
class AuthorizationRequestWrapperTest {

    private lateinit var config: AuthorizationServiceConfiguration

    @Before
    fun setup() {
        config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )
    }

    @Test
    fun testRequestWrapperPassesParametersToNative() {
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client-id",
            scopes = listOf("openid", "profile", "email"),
            responseType = "code",
            redirectUri = "com.example.app:/oauth2redirect",
            additionalParameters = null
        )

        assertNotNull(request.android)
        assertEquals("test-client-id", request.android.clientId)
        assertEquals("code", request.android.responseType)
    }

    @Test
    fun testRequestWrapperPassesAdditionalParameters() {
        val additionalParams = mapOf(
            "prompt" to "consent",
            "access_type" to "offline"
        )

        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client",
            scopes = listOf("openid"),
            responseType = "code",
            redirectUri = "app:/callback",
            additionalParameters = additionalParams
        )

        assertNotNull(request.android)
        val nativeParams = request.android.additionalParameters
        assertEquals("consent", nativeParams["prompt"])
        assertEquals("offline", nativeParams["access_type"])
    }
}

@RunWith(AndroidJUnit4::class)
class TokenRequestWrapperTest {

    private lateinit var config: AuthorizationServiceConfiguration

    @Before
    fun setup() {
        config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )
    }

    @Test
    fun testTokenRequestWrapperForAuthorizationCode() {
        val request = TokenRequest(
            config = config,
            clientId = "test-client",
            grantType = "authorization_code",
            refreshToken = null
        )

        assertNotNull(request.android)
        assertEquals("authorization_code", request.android.grantType)
        assertEquals("test-client", request.android.clientId)
    }

    @Test
    fun testTokenRequestWrapperForRefreshToken() {
        val request = TokenRequest(
            config = config,
            clientId = "test-client",
            grantType = "refresh_token",
            refreshToken = "test-refresh-token"
        )

        assertNotNull(request.android)
        assertEquals("refresh_token", request.android.grantType)
        assertEquals("test-refresh-token", request.android.refreshToken)
    }
}
