package dev.gitlive.appauth

import android.content.ContextWrapper
import android.net.Uri
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenResponse as AndroidTokenResponse

actual typealias AuthorizationException = AuthorizationException

actual typealias AuthorizationServiceContext = ContextWrapper

actual class TokenResponse internal constructor(
    private val androidTokenResponse: AndroidTokenResponse
) {
    actual val idToken: String?
        get() = androidTokenResponse.idToken

    actual val accessToken: String?
        get() = androidTokenResponse.accessToken

    actual val refreshToken: String?
        get() = androidTokenResponse.refreshToken

    override fun toString(): String {
        return buildString {
            appendLine("TokenResponse(")
            appendLine("  accessToken: ${accessToken ?: "None"}")
            appendLine("  idToken: ${idToken ?: "None"}")
            appendLine("  refreshToken: ${refreshToken ?: "None"}")
            appendLine("  tokenType: ${androidTokenResponse.tokenType ?: "None"}")
            appendLine("  scope: ${androidTokenResponse.scope ?: "None"}")
            appendLine("  accessTokenExpirationTime: ${androidTokenResponse.accessTokenExpirationTime ?: "None"}")
            appendLine(")")
        }
    }
}

actual class EndSessionRequest internal constructor(internal val android: net.openid.appauth.EndSessionRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        idTokenHint: String?,
        postLogoutRedirectUri: String?,
        additionalParameters: Map<String, String>?,
    ) : this(
        net.openid.appauth.EndSessionRequest.Builder(config.android).apply {
            idTokenHint?.let { setIdTokenHint(it) }
            postLogoutRedirectUri?.let { setPostLogoutRedirectUri(Uri.parse(postLogoutRedirectUri)) }
            setAdditionalParameters(additionalParameters)
        }.build()
    )
    override fun toString(): String {
        return buildString {
            appendLine("EndSessionRequest(")
            appendLine("  idTokenHint: ${android.idTokenHint ?: "None"}")
            appendLine("  postLogoutRedirectUri: ${android.postLogoutRedirectUri ?: "None"}")
            appendLine("  state: ${android.state ?: "None"}")
            appendLine("  additionalParameters: ${android.additionalParameters ?: "None"}")
            appendLine("  config:")
            appendLine("    endSessionEndpoint: ${android.configuration.endSessionEndpoint}")
            appendLine(")")
        }
    }
}

actual class RevokeTokenRequest actual constructor(
    val config: AuthorizationServiceConfiguration,
    val token: String,
    val clientId: String,
    val clientSecret: String?
)

