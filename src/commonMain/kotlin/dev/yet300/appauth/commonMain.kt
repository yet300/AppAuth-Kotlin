package dev.yet300.appauth

expect class AuthorizationException : Exception

expect class AuthorizationServiceContext

/**
 * A service that handles the authorization flow.
 * It provides methods for performing authorization, token exchange, and session management.
 * This class is a KMP wrapper around the native AppAuth libraries.
 */
expect class AuthorizationService(
    context: () -> AuthorizationServiceContext,
) {
    /**
     * Performs an authorization request to the authorization server.
     * This typically involves opening a browser or a custom tab for the user to sign in.
     *
     * @param request The [AuthorizationRequest] containing all details for the authorization flow.
     * @return An [AuthorizationResponse] containing the authorization code and other details.
     * @throws AuthorizationException if the authorization flow fails.
     */
    suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse

    /**
     * Performs an end-session request to the authorization server to log the user out.
     * This follows the OIDC RP-Initiated Logout specification.
     *
     * @param request The [EndSessionRequest] containing details for the logout flow.
     * @throws AuthorizationException if the end-session flow fails.
     */
    suspend fun performEndSessionRequest(request: EndSessionRequest)

    /**
     * Performs a token request to exchange an authorization code or refresh token for new tokens.
     *
     * @param request The [TokenRequest] containing the grant type and relevant codes/tokens.
     * @return A [TokenResponse] containing the new access, refresh, and ID tokens.
     * @throws AuthorizationException if the token exchange fails.
     */
    suspend fun performTokenRequest(request: TokenRequest): TokenResponse

    /**
     * Performs a token revocation request as per RFC 7009.
     * This directly communicates with the revocation endpoint to invalidate a token.
     *
     * @param request The [RevokeTokenRequest] containing the token to be revoked.
     * @throws AuthorizationException if the revocation fails.
     */
    suspend fun performRevokeTokenRequest(request: RevokeTokenRequest)
}

/**
 * Represents the configuration of an authorization service, including all necessary endpoints.
 *
 * @property authorizationEndpoint The URL of the authorization endpoint.
 * @property tokenEndpoint The URL of the token endpoint.
 * @property registrationEndpoint The URL of the dynamic client registration endpoint, if available.
 * @property endSessionEndpoint The URL of the end-session (logout) endpoint, if available.
 * @property revocationEndpoint The URL of the token revocation endpoint (RFC 7009), if available.
 */
expect class AuthorizationServiceConfiguration(
    authorizationEndpoint: String,
    tokenEndpoint: String,
    registrationEndpoint: String? = null,
    endSessionEndpoint: String? = null,
    revocationEndpoint: String? = null,
) {
    val authorizationEndpoint: String
    val tokenEndpoint: String
    val registrationEndpoint: String?
    val endSessionEndpoint: String?
    val revocationEndpoint: String?

    companion object {
        /**
         * Fetches the service configuration from an OIDC discovery document.
         *
         * @param url The issuer URL of the authorization server. The discovery document is typically
         *            found at `[url]/.well-known/openid-configuration`.
         * @return A new [AuthorizationServiceConfiguration] instance.
         * @throws Exception if the discovery document cannot be fetched or parsed.
         */
        suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration
    }
}

expect class AuthorizationRequest(
    config: AuthorizationServiceConfiguration,
    clientId: String,
    scopes: List<String>,
    responseType: String,
    redirectUri: String,
    additionalParameters: Map<String, String>?,
)

expect class AuthorizationResponse {
    val idToken: String?
    val authorizationCode: String?
    val scope: String?

    fun createTokenExchangeRequest(): TokenRequest
}

expect class TokenRequest(
    config: AuthorizationServiceConfiguration,
    clientId: String,
    grantType: String,
    refreshToken: String? = null,
)

expect class TokenResponse {
    val idToken: String?
    val accessToken: String?
    val refreshToken: String?
}

expect class EndSessionRequest(
    config: AuthorizationServiceConfiguration,
    idTokenHint: String? = null,
    postLogoutRedirectUri: String? = null,
    additionalParameters: Map<String, String>? = null,
)

/**
 * Encapsulates a request to revoke a token at the token revocation endpoint.
 *
 * @param config The service configuration, which should contain the `revocationEndpoint`.
 * @param token The refresh or access token to be revoked.
 * @param clientId The client ID of the application.
 * @param clientSecret The client secret, if the client is confidential. If provided, it will be
 *                     used for Basic HTTP authentication. If null, the client is treated as public.
 */
expect class RevokeTokenRequest(
    config: AuthorizationServiceConfiguration,
    token: String,
    clientId: String,
    clientSecret: String? = null,
)
