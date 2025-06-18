package dev.gitlive.appauth

expect class AuthorizationException : Exception

expect class AuthorizationServiceContext

expect class AuthorizationService(context: () -> AuthorizationServiceContext) {
    suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse
    suspend fun performEndSessionRequest(request: EndSessionRequest)
    suspend fun performTokenRequest(request: TokenRequest): TokenResponse
    suspend fun performRevokeTokenRequest(request: RevokeTokenRequest)
}

expect class AuthorizationServiceConfiguration(
    authorizationEndpoint: String,
    tokenEndpoint: String,
    registrationEndpoint: String? = null,
    endSessionEndpoint: String? = null,
    revocationEndpoint: String? = null
) {
    val authorizationEndpoint: String
    val tokenEndpoint: String
    val registrationEndpoint: String?
    val endSessionEndpoint: String?
    val revocationEndpoint: String?

    companion object {
        suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration
    }
}

expect class AuthorizationRequest(
    config: AuthorizationServiceConfiguration,
    clientId: String,
    scopes: List<String>,
    responseType: String,
    redirectUri: String,
    additionalParameters: Map<String, String>?
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
    refreshToken: String? = null
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


expect class RevokeTokenRequest(
    config: AuthorizationServiceConfiguration,
    token: String,
    clientId: String,
    clientSecret: String? = null
)