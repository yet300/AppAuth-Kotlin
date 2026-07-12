package dev.yet300.appauth

/**
 * Interactive OAuth flows that require a browser, custom tab, or platform UI context.
 */
interface InteractiveAuthorization {
    suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse

    suspend fun performEndSessionRequest(request: EndSessionRequest)
}
