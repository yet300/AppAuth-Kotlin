@file:OptIn(ExperimentalForeignApi::class)

package dev.yet300.appauth

import AppAuth.OIDAuthorizationRequest
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

actual class AuthorizationRequest private constructor(
    internal val ios: OIDAuthorizationRequest,
) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        scopes: List<String>,
        responseType: String,
        redirectUri: String,
        additionalParameters: Map<String, String>?,
    ) : this(
        OIDAuthorizationRequest(
            configuration = config.ios,
            clientId = clientId,
            scopes = scopes,
            redirectURL = NSURL.URLWithString(redirectUri)!!,
            responseType = responseType,
            additionalParameters = additionalParameters as Map<Any?, *>?,
        ),
    )

    @OptIn(ExperimentalForeignApi::class)
    override fun toString(): String =
        buildString {
            appendLine("AuthorizationRequest(")
            appendLine("  clientId: ${ios.clientID()}")
            appendLine("  scope: ${ios.scope() ?: "None"}")
            appendLine("  responseType: ${ios.responseType()}")
            appendLine("  redirectUri: ${ios.redirectURL()?.absoluteString ?: "None"}")
            appendLine("  additionalParameters: ${ios.additionalParameters() ?: "None"}")
            appendLine("  config:")
            appendLine("    authorizationEndpoint: ${ios.configuration().authorizationEndpoint().absoluteString}")
            appendLine("    tokenEndpoint: ${ios.configuration().tokenEndpoint().absoluteString}")
            appendLine(")")
        }
}
