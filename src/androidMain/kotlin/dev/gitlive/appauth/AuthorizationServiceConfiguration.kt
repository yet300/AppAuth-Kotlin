package dev.gitlive.appauth

import android.net.Uri
import io.github.aakira.napier.Napier
import net.openid.appauth.AuthorizationException
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// wrap network errors in an IOException so it matches ktor
internal fun net.openid.appauth.AuthorizationException.wrapIfNecessary() =
    takeUnless { it == AuthorizationException.GeneralErrors.NETWORK_ERROR }
        ?: IOException(message, this)

actual class AuthorizationServiceConfiguration private constructor(
    val android: net.openid.appauth.AuthorizationServiceConfiguration,
    actual val revocationEndpoint: String?
) {

    actual constructor(
        authorizationEndpoint: String,
        tokenEndpoint: String,
        registrationEndpoint: String?,
        endSessionEndpoint: String?,
        revocationEndpoint: String?
    ) : this(
        net.openid.appauth.AuthorizationServiceConfiguration(
            Uri.parse(authorizationEndpoint),
            Uri.parse(tokenEndpoint),
            registrationEndpoint?.let { Uri.parse(it) },
            endSessionEndpoint?.let { Uri.parse(it) },
        ),
        revocationEndpoint
    )

    actual companion object {
        actual suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration =
            suspendCoroutine { cont ->
                Napier.d("🌐 Starting custom fetchFromIssuer for Android")
                net.openid.appauth.AuthorizationServiceConfiguration.fetchFromIssuer(
                    Uri.parse(url)
                ) { serviceConfiguration, ex ->
                    if (ex != null) {
                        Napier.e("❌ Failed to fetch base configuration", ex)
                        cont.resumeWithException(ex.wrapIfNecessary())
                        return@fetchFromIssuer
                    }
                    if (serviceConfiguration == null) {
                        Napier.e("❌ Fetched configuration is null")
                        cont.resumeWithException(IllegalStateException("Configuration is null"))
                        return@fetchFromIssuer
                    }

                    var revocationEndpoint: String? = null
                    try {
                        val discoveryDocJson: JSONObject? = serviceConfiguration.discoveryDoc?.docJson
                        if (discoveryDocJson != null && discoveryDocJson.has("revocation_endpoint")) {
                            // Проверяем, что значение не null, прежде чем его получать
                            if (!discoveryDocJson.isNull("revocation_endpoint")) {
                                revocationEndpoint = discoveryDocJson.getString("revocation_endpoint")
                                Napier.i("✅ Found revocation_endpoint: $revocationEndpoint")
                            }
                        }
                    } catch (jsonEx: JSONException) {
                        Napier.w(
                            "Could not parse revocation_endpoint from discovery document",
                            jsonEx
                        )
                    }

                    cont.resume(
                        AuthorizationServiceConfiguration(
                            android = serviceConfiguration,
                            revocationEndpoint = revocationEndpoint
                        )
                    )
                }
            }
    }

    actual val authorizationEndpoint get() = android.authorizationEndpoint.toString()
    actual val tokenEndpoint get() = android.tokenEndpoint.toString()
    actual val registrationEndpoint get() = android.registrationEndpoint?.toString()
    actual val endSessionEndpoint get() = android.endSessionEndpoint?.toString()
}