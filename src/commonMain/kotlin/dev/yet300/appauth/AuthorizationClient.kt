package dev.yet300.appauth

/**
 * Common contract for OAuth / OIDC operations exposed by [AuthorizationService].
 *
 * Apps should depend on this interface (not the platform [AuthorizationService] class)
 * so token refresh, revocation, and interactive login flows can be faked in
 * `commonTest` without constructing a platform service.
 *
 * Composed of [TokenOperations] (headless) and [InteractiveAuthorization] (UI-bound).
 */
interface AuthorizationClient : TokenOperations, InteractiveAuthorization
