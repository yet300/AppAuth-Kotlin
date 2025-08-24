package dev.yet300.appauth

import android.net.Uri

actual class AuthorizationRequest private constructor(internal val android: net.openid.appauth.AuthorizationRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        scopes: List<String>,
        responseType: String,
        redirectUri: String,
        additionalParameters: Map<String, String>?
    ) : this(
        net.openid.appauth.AuthorizationRequest.Builder(
            config.android,
            clientId,
            responseType,
            Uri.parse(redirectUri),
        )
            .setAdditionalParameters(additionalParameters)
            .setScopes(scopes)
            .build()
    )
    override fun toString(): String {
        return buildString {
            appendLine("AuthorizationRequest(")
            appendLine("  clientId: ${android.clientId}")
            appendLine("  scope: ${android.scope ?: "None"}")
            appendLine("  responseType: ${android.responseType}")
            appendLine("  redirectUri: ${android.redirectUri}")
            appendLine("  additionalParameters: ${android.additionalParameters ?: "None"}")
            appendLine("  config:")
            appendLine("    authEndpoint: ${android.configuration.authorizationEndpoint}")
            appendLine("    tokenEndpoint: ${android.configuration.tokenEndpoint}")
            appendLine(")")
        }
    }
}