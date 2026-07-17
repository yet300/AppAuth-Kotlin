package dev.yet300.appauth.testing

import dev.yet300.appauth.AuthorizationClient
import dev.yet300.appauth.AuthorizationRequest
import dev.yet300.appauth.AuthorizationResponse
import dev.yet300.appauth.EndSessionRequest
import dev.yet300.appauth.RevokeTokenRequest
import dev.yet300.appauth.TokenRequest
import dev.yet300.appauth.TokenResponse

/**
 * Configurable fake [AuthorizationClient] for unit tests.
 *
 * Stub responses via the `on*` lambdas. By default, token and authorization
 * handlers throw — override them in tests that need return values.
 *
 * All invocations are recorded in the public `*Calls` lists for ordering assertions
 * (e.g. revoke-before-clear in logout tests).
 */
class FakeAuthorizationClient : AuthorizationClient {
    var onAuthorizationRequest: suspend (AuthorizationRequest) -> AuthorizationResponse = {
        error("performAuthorizationRequest not stubbed")
    }
    var onEndSessionRequest: suspend (EndSessionRequest) -> Unit = {}
    var onTokenRequest: suspend (TokenRequest) -> TokenResponse = {
        error("performTokenRequest not stubbed")
    }
    var onRevokeTokenRequest: suspend (RevokeTokenRequest) -> Unit = {}

    val authorizationCalls = mutableListOf<AuthorizationRequest>()
    val endSessionCalls = mutableListOf<EndSessionRequest>()
    val tokenCalls = mutableListOf<TokenRequest>()
    val revokeCalls = mutableListOf<RevokeTokenRequest>()

    override suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse {
        authorizationCalls += request
        return onAuthorizationRequest(request)
    }

    override suspend fun performEndSessionRequest(request: EndSessionRequest) {
        endSessionCalls += request
        onEndSessionRequest(request)
    }

    override suspend fun performTokenRequest(request: TokenRequest): TokenResponse {
        tokenCalls += request
        return onTokenRequest(request)
    }

    override suspend fun performRevokeTokenRequest(request: RevokeTokenRequest) {
        revokeCalls += request
        onRevokeTokenRequest(request)
    }
}
