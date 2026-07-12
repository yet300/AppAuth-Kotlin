package dev.yet300.appauth

/**
 * Headless OAuth token operations that do not require a browser or platform UI context.
 *
 * Token refresh and revocation can run from background coroutines and are the primary
 * targets for unit tests in consumer apps.
 */
interface TokenOperations {
    suspend fun performTokenRequest(request: TokenRequest): TokenResponse

    suspend fun performRevokeTokenRequest(request: RevokeTokenRequest)
}
