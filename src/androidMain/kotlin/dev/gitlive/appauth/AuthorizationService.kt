package dev.gitlive.appauth

import android.content.Intent
import android.util.Base64
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


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