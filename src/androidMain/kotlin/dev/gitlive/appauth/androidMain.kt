package dev.gitlive.appauth

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.IOException
import net.openid.appauth.AuthorizationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import net.openid.appauth.TokenResponse as AndroidTokenResponse


actual typealias AuthorizationException = AuthorizationException

// wrap network errors in an IOException so it matches ktor
private fun AuthorizationException.wrapIfNecessary() =
    takeUnless { it == AuthorizationException.GeneralErrors.NETWORK_ERROR }
        ?: IOException(message, this)

actual typealias AuthorizationServiceContext = ContextWrapper

actual class AuthorizationService private constructor(private val android: net.openid.appauth.AuthorizationService) {
    actual constructor(context: () -> AuthorizationServiceContext) : this(
        net.openid.appauth.AuthorizationService(
            context()
        )
    )

    fun bind(activityOrFragment: ActivityResultCaller) {
        launcher = activityOrFragment
            .registerForActivityResult(StartActivityForResult()) { result ->
                result.data
                    ?.let { AuthorizationException.fromIntent(it) }
                    ?.let { response.completeExceptionally(it.wrapIfNecessary()) }
                    ?: response.complete(result.data)
            }
    }

    private var response = CompletableDeferred<Intent?>(value = null)

    private lateinit var launcher: ActivityResultLauncher<Intent>

    actual suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse {
        // if a previous request is still pending then wait for it to finish
        response.runCatching { await() }
        response = CompletableDeferred()
        launcher.launch(android.getAuthorizationRequestIntent(request.android))
        return AuthorizationResponse(net.openid.appauth.AuthorizationResponse.fromIntent(response.await()!!)!!)
    }

    actual suspend fun performEndSessionRequest(request: EndSessionRequest): EndSessionResponse {
        // if a previous request is still pending then wait for it to finish
        response.runCatching { await() }
        response = CompletableDeferred()
        launcher.launch(android.getEndSessionRequestIntent(request.android))
        return EndSessionResponse.fromIntent(response.await()!!)!!
    }

    actual suspend fun performTokenRequest(request: TokenRequest): TokenResponse =
        suspendCoroutine { cont ->
            android.performTokenRequest(request.android) { response, ex ->
                response?.let { cont.resume(TokenResponse(it)) }
                    ?: cont.resumeWithException(ex!!.wrapIfNecessary())
            }
        }
}

actual class AuthorizationServiceConfiguration private constructor(
    val android: net.openid.appauth.AuthorizationServiceConfiguration
) {

    actual constructor(
        authorizationEndpoint: String,
        tokenEndpoint: String,
        registrationEndpoint: String?,
        endSessionEndpoint: String?
    ) : this(
        net.openid.appauth.AuthorizationServiceConfiguration(
            Uri.parse(authorizationEndpoint),
            Uri.parse(tokenEndpoint),
            registrationEndpoint?.let { Uri.parse(it) },
            endSessionEndpoint?.let { Uri.parse(it) },
        )
    )

    actual companion object {
        actual suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration =
            suspendCoroutine { cont ->
                net.openid.appauth.AuthorizationServiceConfiguration
                    .fetchFromIssuer(Uri.parse(url)) { serviceConfiguration, ex ->
                        serviceConfiguration?.let {
                            cont.resume(AuthorizationServiceConfiguration(it))
                        }
                            ?: cont.resumeWithException(ex!!.wrapIfNecessary())
                    }
            }
    }

    actual val authorizationEndpoint get() = android.authorizationEndpoint.toString()
    actual val tokenEndpoint get() = android.tokenEndpoint.toString()
    actual val registrationEndpoint get() = android.registrationEndpoint?.toString()
    actual val endSessionEndpoint get() = android.endSessionEndpoint?.toString()
}

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
}

actual class AuthorizationResponse internal constructor(private val android: net.openid.appauth.AuthorizationResponse) {
    actual fun createTokenExchangeRequest() = TokenRequest(android.createTokenExchangeRequest())
    actual val idToken get() = android.idToken
    actual val scope get() = android.scope
    actual val authorizationCode get() = android.authorizationCode
}

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
}

actual class TokenResponse internal constructor(
    private val androidTokenResponse: AndroidTokenResponse
) {
    actual val idToken: String?
        get() = androidTokenResponse.idToken

    actual val accessToken: String?
        get() = androidTokenResponse.accessToken

    actual val refreshToken: String?
        get() = androidTokenResponse.refreshToken
}

actual class EndSessionRequest internal constructor(internal val android: net.openid.appauth.EndSessionRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        idTokenHint: String?,
        postLogoutRedirectUri: String?
    ) : this(
        net.openid.appauth.EndSessionRequest.Builder(config.android).apply {
            idTokenHint?.let { setIdTokenHint(it) }
            postLogoutRedirectUri?.let { setPostLogoutRedirectUri(Uri.parse(postLogoutRedirectUri)) }
        }.build()
    )
}

actual typealias EndSessionResponse = net.openid.appauth.EndSessionResponse
