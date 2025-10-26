package dev.yet300.appauth

/**
 * Test helper functions and utilities for AppAuth tests
 */

/**
 * Creates a standard test configuration with all endpoints
 */
fun createFullTestConfig() = AuthorizationServiceConfiguration(
    authorizationEndpoint = "https://auth.example.com/authorize",
    tokenEndpoint = "https://auth.example.com/token",
    registrationEndpoint = "https://auth.example.com/register",
    endSessionEndpoint = "https://auth.example.com/logout",
    revocationEndpoint = "https://auth.example.com/revoke"
)

/**
 * Creates a minimal test configuration with only required endpoints
 */
fun createMinimalTestConfig() = AuthorizationServiceConfiguration(
    authorizationEndpoint = "https://auth.example.com/authorize",
    tokenEndpoint = "https://auth.example.com/token"
)

/**
 * Creates a standard authorization request for testing
 */
fun createTestAuthorizationRequest(
    config: AuthorizationServiceConfiguration = createMinimalTestConfig(),
    clientId: String = "test-client-id",
    scopes: List<String> = listOf("openid", "profile", "email"),
    additionalParameters: Map<String, String>? = null
) = AuthorizationRequest(
    config = config,
    clientId = clientId,
    scopes = scopes,
    responseType = "code",
    redirectUri = "com.example.app:/oauth2redirect",
    additionalParameters = additionalParameters
)

/**
 * Creates a token request for authorization code exchange
 */
fun createAuthCodeTokenRequest(
    config: AuthorizationServiceConfiguration = createMinimalTestConfig(),
    clientId: String = "test-client-id"
) = TokenRequest(
    config = config,
    clientId = clientId,
    grantType = "authorization_code",
    refreshToken = null
)

/**
 * Creates a token request for refresh token flow
 */
fun createRefreshTokenRequest(
    config: AuthorizationServiceConfiguration = createMinimalTestConfig(),
    clientId: String = "test-client-id",
    refreshToken: String = "test-refresh-token"
) = TokenRequest(
    config = config,
    clientId = clientId,
    grantType = "refresh_token",
    refreshToken = refreshToken
)

/**
 * Creates an end session request for testing
 */
fun createTestEndSessionRequest(
    config: AuthorizationServiceConfiguration = createFullTestConfig(),
    idTokenHint: String? = "test-id-token",
    postLogoutRedirectUri: String? = "com.example.app:/logout"
) = EndSessionRequest(
    config = config,
    idTokenHint = idTokenHint,
    postLogoutRedirectUri = postLogoutRedirectUri,
    additionalParameters = null
)

/**
 * Creates a revoke token request for testing
 */
fun createTestRevokeTokenRequest(
    config: AuthorizationServiceConfiguration = createFullTestConfig(),
    token: String = "test-token",
    clientId: String = "test-client-id",
    clientSecret: String? = null
) = RevokeTokenRequest(
    config = config,
    token = token,
    clientId = clientId,
    clientSecret = clientSecret
)

/**
 * Common test constants
 */
object TestConstants {
    const val TEST_CLIENT_ID = "test-client-id"
    const val TEST_CLIENT_SECRET = "test-client-secret"
    const val TEST_REDIRECT_URI = "com.example.app:/oauth2redirect"
    const val TEST_LOGOUT_REDIRECT_URI = "com.example.app:/logout"
    const val TEST_AUTHORIZATION_CODE = "test-auth-code"
    const val TEST_ACCESS_TOKEN = "test-access-token"
    const val TEST_REFRESH_TOKEN = "test-refresh-token"
    const val TEST_ID_TOKEN = "test-id-token"
    
    const val TEST_AUTH_ENDPOINT = "https://auth.example.com/authorize"
    const val TEST_TOKEN_ENDPOINT = "https://auth.example.com/token"
    const val TEST_REVOCATION_ENDPOINT = "https://auth.example.com/revoke"
    const val TEST_END_SESSION_ENDPOINT = "https://auth.example.com/logout"
    const val TEST_REGISTRATION_ENDPOINT = "https://auth.example.com/register"
    
    val TEST_SCOPES = listOf("openid", "profile", "email")
    val TEST_EXTENDED_SCOPES = listOf("openid", "profile", "email", "address", "phone", "offline_access")
}
