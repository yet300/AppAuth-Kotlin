@file:OptIn(ExperimentalForeignApi::class)

package dev.gitlive.appauth

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import cocoapods.AppAuth.OIDAuthorizationService
import cocoapods.AppAuth.OIDEndSessionRequest
import cocoapods.AppAuth.OIDEndSessionResponse
import cocoapods.AppAuth.OIDErrorCodeNetworkError
import cocoapods.AppAuth.OIDExternalUserAgentIOS
import cocoapods.AppAuth.OIDExternalUserAgentSessionProtocol
import cocoapods.AppAuth.OIDGeneralErrorDomain
import cocoapods.AppAuth.OIDServiceConfiguration
import cocoapods.AppAuth.OIDTokenRequest
import cocoapods.AppAuth.OIDTokenResponse
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import io.github.aakira.napier.Napier

actual class AuthorizationException(message: String?) : Exception(message)

// wrap network errors in an IOException so it matches ktor
@OptIn(ExperimentalForeignApi::class)
private fun NSError.toException() = when (domain) {
    OIDGeneralErrorDomain -> when (code) {
        OIDErrorCodeNetworkError -> IOException(localizedDescription)
        else -> AuthorizationException(localizedDescription)
    }
    else -> AuthorizationException(localizedDescription)
}

actual class AuthorizationServiceConfiguration private constructor(val ios: OIDServiceConfiguration) {

    actual constructor(
        authorizationEndpoint: String,
        tokenEndpoint: String,
        registrationEndpoint: String?,
        endSessionEndpoint: String?,
        revocationEndpoint: String?
    ) : this(
        OIDServiceConfiguration(
            NSURL.URLWithString(authorizationEndpoint)!!,
            NSURL.URLWithString(tokenEndpoint)!!,
            null,
            registrationEndpoint?.let { NSURL.URLWithString(it) },
            endSessionEndpoint?.let { NSURL.URLWithString(it) }
        )
    )

    actual companion object {
        @OptIn(ExperimentalForeignApi::class)
        actual suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration =
            suspendCoroutine { cont ->
                Napier.d("🌐 Starting iOS fetchFromIssuer")
                Napier.d("🔗 Issuer URL: $url")

                val nsUrl = NSURL.URLWithString(url)
                if (nsUrl == null) {
                    Napier.e("❌ Invalid URL: $url")
                    cont.resumeWithException(IllegalArgumentException("Invalid issuer URL: $url"))
                    return@suspendCoroutine
                }

                OIDAuthorizationService.discoverServiceConfigurationForIssuer(nsUrl) { config, error ->
                    Napier.d("🔁 Discovery callback triggered")

                    if (config != null) {
                        Napier.d("✅ Discovery successful")
                        Napier.d("📥 AuthorizationServiceConfiguration:")
                        Napier.d("  authorizationEndpoint: ${config.authorizationEndpoint.absoluteString}")
                        Napier.d("  tokenEndpoint: ${config.tokenEndpoint.absoluteString}")
                        Napier.d("  endSessionEndpoint: ${config.endSessionEndpoint?.absoluteString ?: "None"}")

                        cont.resume(AuthorizationServiceConfiguration(config))
                    } else {
                        Napier.e("❌ Discovery failed: ${error?.localizedDescription}", error!!.toException())
                        cont.resumeWithException(error.toException())
                    }
                }
            }
    }

    actual val authorizationEndpoint: String get() = ios.authorizationEndpoint.relativeString
    actual val tokenEndpoint: String get() = ios.tokenEndpoint.relativeString
    actual val registrationEndpoint: String? get() = ios.registrationEndpoint?.relativeString
    actual val endSessionEndpoint: String? get() = ios.endSessionEndpoint?.relativeString
    actual val revocationEndpoint: String? get() = ios.revocationEndpoint?.absoluteString
}

actual class AuthorizationRequest private constructor(internal val ios: OIDAuthorizationRequest) {

    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        scopes: List<String>,
        responseType: String,
        redirectUri: String,
        additionalParameters: Map<String, String>?
    ) : this(
        OIDAuthorizationRequest(
            configuration = config.ios,
            clientId = clientId,
            scopes = scopes,
            redirectURL = NSURL.URLWithString(redirectUri)!!,
            responseType = responseType,
            additionalParameters = additionalParameters as Map<Any?, *>?,
        )
    )
    @OptIn(ExperimentalForeignApi::class)
    override fun toString(): String {
        return buildString {
            appendLine("AuthorizationRequest(")
            appendLine("  clientId: ${ios.clientID}")
            appendLine("  scope: ${ios.scope ?: "None"}")
            appendLine("  responseType: ${ios.responseType}")
            appendLine("  redirectUri: ${ios.redirectURL?.absoluteString ?: "None"}")
            appendLine("  additionalParameters: ${ios.additionalParameters ?: "None"}")
            appendLine("  config:")
            appendLine("    authorizationEndpoint: ${ios.configuration.authorizationEndpoint.absoluteString}")
            appendLine("    tokenEndpoint: ${ios.configuration.tokenEndpoint.absoluteString}")
            appendLine(")")
        }
    }
}

actual class TokenRequest internal constructor(internal val ios: OIDTokenRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        clientId: String,
        grantType: String,
        refreshToken: String?
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
            additionalParameters = null
        )
    )
    @OptIn(ExperimentalForeignApi::class)
    override fun toString(): String {
        return buildString {
            appendLine("TokenRequest(")
            appendLine("  clientId: ${ios.clientID ?: "None"}")
            appendLine("  grantType: ${ios.grantType}")
            appendLine("  scope: ${ios.scope ?: "None"}")
            appendLine("  refreshToken: ${ios.refreshToken ?: "None"}")
            appendLine("  redirectUri: ${ios.redirectURL?.absoluteString ?: "None"}")
            appendLine("  additionalParameters: ${ios.additionalParameters ?: "None"}")
            appendLine("  config:")
            appendLine("    tokenEndpoint: ${ios.configuration.tokenEndpoint.absoluteString}")
            appendLine("    authorizationEndpoint: ${ios.configuration.authorizationEndpoint.absoluteString}")
            appendLine(")")
        }
    }
}
@OptIn(ExperimentalForeignApi::class)
actual class AuthorizationResponse internal constructor(internal val ios: OIDAuthorizationResponse) {
    actual val authorizationCode: String? get() = ios.authorizationCode
    actual val idToken: String? get() = ios.idToken
    actual val scope get() = ios.scope
    actual fun createTokenExchangeRequest() = TokenRequest(ios.tokenExchangeRequest()!!)

    override fun toString(): String {
        return buildString {
            appendLine("AuthorizationResponse(")
            appendLine("  authorizationCode: ${authorizationCode ?: "None"}")
            appendLine("  idToken: ${idToken ?: "None"}")
            appendLine("  scope: ${scope ?: "None"}")
            appendLine("  state: ${ios.state ?: "None"}")
            appendLine("  redirectUri: ${ios.request?.redirectURL?.absoluteString ?: "None"}")
            appendLine("  clientId: ${ios.request?.clientID ?: "None"}")
            appendLine("  responseType: ${ios.request?.responseType ?: "None"}")
            appendLine("  config:")
            appendLine("    authorizationEndpoint: ${ios.request?.configuration?.authorizationEndpoint?.absoluteString ?: "None"}")
            appendLine("    tokenEndpoint: ${ios.request?.configuration?.tokenEndpoint?.absoluteString ?: "None"}")
            appendLine(")")
        }
    }
}

actual class EndSessionRequest internal constructor(internal val ios: OIDEndSessionRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        idTokenHint: String?,
        postLogoutRedirectUri: String?,
    ) : this(
        OIDEndSessionRequest(
            configuration = config.ios,
            idTokenHint = idTokenHint ?: "",
            postLogoutRedirectURL = postLogoutRedirectUri?.let { uri ->
                NSURL.URLWithString(uri)
                    ?: throw IllegalArgumentException("Invalid postLogoutRedirectUri: $uri")
            } ?: NSURL.URLWithString(postLogoutRedirectUri ?: "")!!,
            additionalParameters = null
        )
    )
}

actual class TokenResponse internal constructor(internal val ios: OIDTokenResponse) {
    actual val idToken: String? get() = ios.idToken
    actual val accessToken: String? get() = ios.accessToken
    actual val refreshToken: String? get() = ios.refreshToken
}


actual typealias AuthorizationServiceContext = UIViewController

@OptIn(ExperimentalForeignApi::class)
actual class AuthorizationService actual constructor(private val context: () -> AuthorizationServiceContext) {

    private var session: OIDExternalUserAgentSessionProtocol? = null

    fun resumeExternalUserAgentFlow(url: NSURL): Boolean =
        session?.resumeExternalUserAgentFlowWithURL(url) == true

    actual suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse =
        withContext(Dispatchers.Main) {
            Napier.d("🔐 Starting iOS performAuthorizationRequest")
            Napier.d("📤 AuthorizationRequest:\n$request")

            suspendCoroutine { cont ->
                val viewController = context()
                Napier.d("🧭 Presenting authorization from context: ${viewController::class.simpleName}")

                session = OIDAuthorizationService.presentAuthorizationRequest(
                    request.ios,
                    OIDExternalUserAgentIOS(viewController)
                ) { response, error ->
                    Napier.d("🔁 Authorization callback triggered")
                    session = null

                    if (response != null) {
                        Napier.d("✅ Authorization successful")
                        Napier.d("📥 AuthorizationResponse:\n$response")
                        cont.resume(AuthorizationResponse(response))
                    } else {
                        Napier.e("❌ Authorization failed: ${error?.localizedDescription}", error!!.toException())
                        cont.resumeWithException(error.toException())
                    }
                }
            }
        }


    actual suspend fun performEndSessionRequest(request: EndSessionRequest) =
        withContext(Dispatchers.Main) {
            Napier.d("🔐 Starting iOS performEndSessionRequest")
            Napier.d("📤 EndSessionRequest:\n$request")

            suspendCoroutine { cont ->
                val viewController = context()
                Napier.d("🧭 Presenting end session from context: ${viewController::class.simpleName}")

                session = OIDAuthorizationService.presentEndSessionRequest(
                    request.ios,
                    OIDExternalUserAgentIOS(viewController)
                ) { response, error ->
                    Napier.d("🔁 End session callback triggered")
                    session = null

                    if (response != null) {
                        Napier.d("✅ End session completed successfully")
                        Napier.d("📥 EndSessionResponse: $response")
                        cont.resume(Unit)
                    } else {
                        Napier.e("❌ End session failed: ${error?.localizedDescription}", error!!.toException())
                        cont.resumeWithException(error.toException())
                    }
                }
            }
        }


    actual suspend fun performTokenRequest(request: TokenRequest): TokenResponse =
        withContext(Dispatchers.Main) {
            Napier.d("🔐 Starting iOS performTokenRequest")
            Napier.d("📤 TokenRequest:\n$request")

            suspendCoroutine { cont ->
                Napier.d("📡 Performing token request via OIDAuthorizationService")

                OIDAuthorizationService.performTokenRequest(request.ios) { response, error ->
                    Napier.d("🔁 Token request callback triggered")

                    if (response != null) {
                        Napier.d("✅ Token request successful")
                        Napier.d("📥 TokenResponse: ${TokenResponse(response)}")
                        cont.resume(TokenResponse(response))
                    } else {
                        Napier.e("❌ Token request failed: ${error?.localizedDescription}", error!!.toException())
                        cont.resumeWithException(error.toException())
                    }
                }
            }
        }

    actual suspend fun performTokenRevocationRequest(request: TokenRevocationRequest) {

    }
}
