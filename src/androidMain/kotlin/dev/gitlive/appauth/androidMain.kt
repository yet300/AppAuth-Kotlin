package dev.gitlive.appauth

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import net.openid.appauth.AuthorizationException
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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
        // Show the request details
        Napier.d("📤 Starting AuthorizationRequest:\n${request}")
        // if a previous request is still pending then wait for it to finish
        response.runCatching {
            Napier.d("⏳ Waiting for previous authorization request to complete...")
            await()
        }
        response = CompletableDeferred()
        Napier.d("🚀 Launching authorization intent")
        launcher.launch(android.getAuthorizationRequestIntent(request.android))
        // Wait for the result and process it
        return try {
            val intent = response.await()
            Napier.d("✅ Authorization response received")

            val rawResponse = net.openid.appauth.AuthorizationResponse.fromIntent(intent!!)
            Napier.d("📥 Parsed AuthorizationResponse:\n$rawResponse")

            AuthorizationResponse(rawResponse!!)
        } catch (e: Exception) {
            Napier.e("❌ Authorization failed", e)
            throw e
        }
    }

    actual suspend fun performEndSessionRequest(request: EndSessionRequest) {
        // if a previous request is still pending then wait for it to finish
       // Show the request details
        Napier.d("📤 Starting EndSessionRequest:\n$request")

        // If a previous request is still pending, wait for it
        response.runCatching {
            Napier.d("⏳ Waiting for previous end session request to complete...")
            await()
        }

        // Prepare for the new request
        response = CompletableDeferred()

        // Launch the logout intent
        Napier.d("🚀 Launching end session intent")
        launcher.launch(android.getEndSessionRequestIntent(request.android))

        // Await the result and parse the response
        return try {
            val intent = response.await()
            Napier.d("✅ End session response received")

            return
        } catch (e: Exception) {
            Napier.e("❌ End session failed", e)
            throw e
        }
    }

    actual suspend fun performTokenRequest(request: TokenRequest): TokenResponse =
        suspendCoroutine { cont ->
        Napier.d("🔐 Starting performTokenRequest")
        Napier.d("📤 TokenRequest:\n$request")

            android.performTokenRequest(request.android) { response, ex ->
                if (response != null) {
                    Napier.d("✅ Token response received")
                    Napier.d("📥 Parsed TokenResponse:\n$response")
                    cont.resume(TokenResponse(response))
                } else {
                    Napier.e("❌ Token request failed", ex)
                    cont.resumeWithException(ex!!.wrapIfNecessary())
                }
            }
        }

    actual suspend fun performRevokeTokenRequest(request: RevokeTokenRequest) =
        withContext(Dispatchers.IO) {
            val endpoint = request.config.revocationEndpoint
                ?: throw IllegalStateException("Revocation endpoint not found in configuration.")

            var connection: HttpURLConnection? = null
            try {
                Napier.d("Performing token revocation via native HttpURLConnection to $endpoint")
                connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val params = mutableMapOf("token" to request.token)
                if (request.clientSecret == null) {
                    params["client_id"] = request.clientId
                } else {
                    val credentials = "${request.clientId}:${request.clientSecret}"
                    val authHeader =
                        "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
                    connection.setRequestProperty("Authorization", authHeader)
                }

                val writer = BufferedWriter(OutputStreamWriter(connection.outputStream, "UTF-8"))
                writer.write(getPostDataString(params))
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                Napier.d("Revocation response code: $responseCode")


                if (responseCode >= 400) {
                    val errorStream = connection.errorStream?.bufferedReader()?.readText()
                    throw AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.SERVER_ERROR,
                        Exception("Revocation failed with code $responseCode: $errorStream")
                    )
                }

            } catch (e: Exception) {
                Napier.e("Revocation request failed", e)
                throw e
            } finally {
                connection?.disconnect()
            }
        }

    private fun getPostDataString(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
    }

}

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

actual class AuthorizationResponse internal constructor(private val android: net.openid.appauth.AuthorizationResponse) {
    actual fun createTokenExchangeRequest() = TokenRequest(android.createTokenExchangeRequest())
    actual val idToken get() = android.idToken
    actual val scope get() = android.scope
    actual val authorizationCode get() = android.authorizationCode

    override fun toString(): String {
        return buildString {
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

