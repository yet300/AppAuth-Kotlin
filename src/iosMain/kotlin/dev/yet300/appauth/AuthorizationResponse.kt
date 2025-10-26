package dev.yet300.appauth

import AppAuth.OIDAuthorizationResponse
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class AuthorizationResponse internal constructor(
    internal val ios: OIDAuthorizationResponse,
) {
    actual val authorizationCode: String? get() = ios.authorizationCode()
    actual val idToken: String? get() = ios.idToken()
    actual val scope get() = ios.scope()

    actual fun createTokenExchangeRequest() = TokenRequest(ios.tokenExchangeRequest()!!)

    override fun toString(): String =
        buildString {
            appendLine("AuthorizationResponse(")
            appendLine("  authorizationCode: ${authorizationCode ?: "None"}")
            appendLine("  idToken: ${idToken ?: "None"}")
            appendLine("  scope: ${scope ?: "None"}")
            appendLine("  state: ${ios.state() ?: "None"}")
            appendLine("  redirectUri: ${ios.request().redirectURL()?.absoluteString ?: "None"}")
            appendLine("  clientId: ${ios.request().clientID()}")
            appendLine("  responseType: ${ios.request().responseType()}")
            appendLine("  config:")
            appendLine(
                "    authorizationEndpoint: ${ios
                    .request()
                    .configuration()
                    .authorizationEndpoint()
                    .absoluteString ?: "None"}",
            )
            appendLine(
                "    tokenEndpoint: ${ios
                    .request()
                    .configuration()
                    .tokenEndpoint()
                    .absoluteString ?: "None"}",
            )
            appendLine(")")
        }
}
