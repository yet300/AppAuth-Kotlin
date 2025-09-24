package dev.yet300.appauth

import AppAuth.OIDAuthorizationService
import AppAuth.OIDServiceConfiguration
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class AuthorizationServiceConfiguration private constructor(
    val ios: OIDServiceConfiguration,
    actual val revocationEndpoint: String?,
) {

    actual constructor(
        authorizationEndpoint: String,
        tokenEndpoint: String,
        registrationEndpoint: String?,
        endSessionEndpoint: String?,
        revocationEndpoint: String?,
    ) : this(
        OIDServiceConfiguration(
            NSURL.URLWithString(authorizationEndpoint)!!,
            NSURL.URLWithString(tokenEndpoint)!!,
            null,
            registrationEndpoint?.let { NSURL.URLWithString(it) },
            endSessionEndpoint?.let { NSURL.URLWithString(it) },
        ),
        revocationEndpoint,
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
                        Napier.d("  authorizationEndpoint: ${config.authorizationEndpoint().absoluteString}")
                        Napier.d("  tokenEndpoint: ${config.tokenEndpoint().absoluteString}")
                        Napier.d("  endSessionEndpoint: ${config.endSessionEndpoint()?.absoluteString ?: "None"}")

                        var revocationEndpoint: String? = null
                        try {
                            val discoveryDoc = config.discoveryDocument()
                            if (discoveryDoc is Map<*, *>) {
                                revocationEndpoint = discoveryDoc["revocation_endpoint"] as? String
                                if (revocationEndpoint != null) {
                                    Napier.i("✅ Found revocation_endpoint: $revocationEndpoint")
                                }
                            }
                        } catch (e: Exception) {
                            Napier.w("Could not parse revocation_endpoint", e)
                        }

                        cont.resume(AuthorizationServiceConfiguration(config, revocationEndpoint))
                    } else {
                        Napier.e("❌ Discovery failed: ${error?.localizedDescription}", error!!.toException())
                        cont.resumeWithException(error.toException())
                    }
                }
            }
    }

    actual val authorizationEndpoint: String get() = ios.authorizationEndpoint().relativeString
    actual val tokenEndpoint: String get() = ios.tokenEndpoint().relativeString
    actual val registrationEndpoint: String? get() = ios.registrationEndpoint()?.relativeString
    actual val endSessionEndpoint: String? get() = ios.endSessionEndpoint()?.relativeString
}
