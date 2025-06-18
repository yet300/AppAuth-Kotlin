package dev.gitlive.appauth

actual class TokenRequest internal constructor(internal val android: net.openid.appauth.TokenRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        grantType: String,
        refreshToken: String?
    ) : this(
        net.openid.appauth.TokenRequest.Builder(config.android, clientId).apply {
            setGrantType(grantType)
            refreshToken?.let { setRefreshToken(it) }
        }.build()
    )
    override fun toString(): String {
        return buildString {
            appendLine("TokenRequest(")
            appendLine("  clientId: ${android.clientId}")
            appendLine("  grantType: ${android.grantType}")
            appendLine("  scope: ${android.scope ?: "None"}")
            appendLine("  refreshToken: ${android.refreshToken ?: "None"}")
            appendLine("  redirectUri: ${android.redirectUri ?: "None"}")
            appendLine("  additionalParameters: ${android.additionalParameters ?: "None"}")
            appendLine("  config:")
            appendLine("    tokenEndpoint: ${android.configuration.tokenEndpoint}")
            appendLine("    authEndpoint: ${android.configuration.authorizationEndpoint}")
            appendLine(")")
        }
    }
}