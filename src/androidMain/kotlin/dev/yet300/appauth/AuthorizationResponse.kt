package dev.yet300.appauth

actual class AuthorizationResponse internal constructor(
    private val android: net.openid.appauth.AuthorizationResponse,
) {
    actual fun createTokenExchangeRequest() = TokenRequest(android.createTokenExchangeRequest())

    actual val idToken get() = android.idToken
    actual val scope get() = android.scope
    actual val authorizationCode get() = android.authorizationCode

    override fun toString(): String =
        buildString {
            appendLine("AuthorizationResponse(")
            appendLine("  authorizationCode: ${authorizationCode ?: "None"}")
            appendLine("  idToken: ${idToken ?: "None"}")
            appendLine("  scope: ${scope ?: "None"}")
            appendLine("  state: ${android.state ?: "None"}")
            appendLine("  tokenExchangeRequest:")
            appendLine("    clientId: ${android.request.clientId}")
            appendLine("    redirectUri: ${android.request.redirectUri}")
            appendLine("    responseType: ${android.request.responseType}")
            appendLine(")")
        }
}
