@file:OptIn(ExperimentalForeignApi::class)

package dev.yet300.appauth

import AppAuth.OIDTokenRequest
import kotlinx.cinterop.ExperimentalForeignApi

actual class TokenRequest internal constructor(
    internal val ios: OIDTokenRequest,
) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        grantType: String,
        refreshToken: String?,
    ) : this(
        OIDTokenRequest(
            configuration = config.ios,
            grantType = grantType,
            authorizationCode = null,
            redirectURL = null,
            clientID = clientId,
            clientSecret = null,
            scope = null,
            refreshToken = refreshToken,
            codeVerifier = null,
            additionalParameters = null,
        ),
    )

    @OptIn(ExperimentalForeignApi::class)
    override fun toString(): String =
        buildString {
            appendLine("TokenRequest(")
            appendLine("  clientId: ${ios.clientID()}")
            appendLine("  grantType: ${ios.grantType()}")
            appendLine("  scope: ${ios.scope() ?: "None"}")
            appendLine("  refreshToken: ${ios.refreshToken() ?: "None"}")
            appendLine("  redirectUri: ${ios.redirectURL()?.absoluteString ?: "None"}")
            appendLine("  additionalParameters: ${ios.additionalParameters() ?: "None"}")
            appendLine("  config:")
            appendLine("    tokenEndpoint: ${ios.configuration().tokenEndpoint().absoluteString}")
            appendLine("    authorizationEndpoint: ${ios.configuration().authorizationEndpoint().absoluteString}")
            appendLine(")")
        }
}
