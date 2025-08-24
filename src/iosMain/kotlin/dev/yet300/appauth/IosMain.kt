@file:OptIn(ExperimentalForeignApi::class)

package dev.yet300.appauth

import cocoapods.AppAuth.OIDEndSessionRequest
import cocoapods.AppAuth.OIDErrorCodeNetworkError
import cocoapods.AppAuth.OIDGeneralErrorDomain
import cocoapods.AppAuth.OIDTokenResponse
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.IOException
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

actual class AuthorizationException(message: String?) : Exception(message)

// wrap network errors in an IOException so it matches ktor
@OptIn(ExperimentalForeignApi::class)
internal fun NSError.toException() = when (domain) {
    OIDGeneralErrorDomain -> when (code) {
        OIDErrorCodeNetworkError -> IOException(localizedDescription)
        else -> AuthorizationException(localizedDescription)
    }
    else -> AuthorizationException(localizedDescription)
}

actual class EndSessionRequest internal constructor(internal val ios: OIDEndSessionRequest) {
    actual constructor(
        config: AuthorizationServiceConfiguration,
        idTokenHint: String?,
        postLogoutRedirectUri: String?,
        additionalParameters: Map<String, String>?,
    ) : this(
        OIDEndSessionRequest(
            configuration = config.ios,
            idTokenHint = idTokenHint ?: "",
            postLogoutRedirectURL = postLogoutRedirectUri?.let { uri ->
                NSURL.URLWithString(uri)
                    ?: throw IllegalArgumentException("Invalid postLogoutRedirectUri: $uri")
            } ?: NSURL.URLWithString(postLogoutRedirectUri ?: "")!!,
            additionalParameters = additionalParameters?.mapValues { it.value as Any? },
        ),
    )
}

actual class TokenResponse internal constructor(internal val ios: OIDTokenResponse) {
    actual val idToken: String? get() = ios.idToken
    actual val accessToken: String? get() = ios.accessToken
    actual val refreshToken: String? get() = ios.refreshToken
}

actual typealias AuthorizationServiceContext = UIViewController

actual class RevokeTokenRequest actual constructor(
    val config: AuthorizationServiceConfiguration,
    val token: String,
    val clientId: String,
    val clientSecret: String?,
)
