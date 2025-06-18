package dev.gitlive.appauth

import cocoapods.AppAuth.OIDAuthorizationService
import cocoapods.AppAuth.OIDExternalUserAgentIOS
import cocoapods.AppAuth.OIDExternalUserAgentSessionProtocol
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine



@OptIn(ExperimentalForeignApi::class)
internal fun String.base64Encoded(): String {
    val data = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    return data?.base64EncodedStringWithOptions(0u) ?: ""
}


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
                        Napier.e(
                            "❌ Authorization failed: ${error?.localizedDescription}",
                            error!!.toException()
                        )
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
                        Napier.e(
                            "❌ End session failed: ${error?.localizedDescription}",
                            error!!.toException()
                        )
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
                        Napier.e(
                            "❌ Token request failed: ${error?.localizedDescription}",
                            error!!.toException()
                        )
                        cont.resumeWithException(error.toException())
                    }
                }
            }
        }

    actual suspend fun performRevokeTokenRequest(request: RevokeTokenRequest) {
        val endpoint = request.config.revocationEndpoint
            ?: throw AuthorizationException("Revocation endpoint not found in configuration.")

        return suspendCoroutine { continuation ->
            Napier.d("Performing token revocation via native URLSession to $endpoint")

            val url = NSURL(string = endpoint)
            val urlRequest = NSMutableURLRequest(uRL = url)

            urlRequest.setHTTPMethod("POST")

            urlRequest.setValue(
                "application/x-www-form-urlencoded",
                forHTTPHeaderField = "Content-Type"
            )

            var bodyString = "token=${request.token.urlEncoded()}"
            if (request.clientSecret == null) {
                bodyString += "&client_id=${request.clientId.urlEncoded()}"
            } else {
                val credentials = "${request.clientId}:${request.clientSecret}"
                val authHeader = "Basic ${credentials.base64Encoded()}"
                urlRequest.setValue(authHeader, forHTTPHeaderField = "Authorization")
            }

            urlRequest.setHTTPBody((bodyString as NSString).dataUsingEncoding(NSUTF8StringEncoding))

            val task =
                NSURLSession.sharedSession.dataTaskWithRequest(urlRequest) { data, response, error ->
                    if (error != null) {
                        continuation.resumeWithException(error.toException())
                        return@dataTaskWithRequest
                    }

                    val httpResponse = response as? NSHTTPURLResponse
                    val statusCode = httpResponse?.statusCode?.toInt() ?: 0
                    Napier.d("Revocation response code: $statusCode")

                    if (statusCode >= 400) {
                        val errorBody = data?.let { NSString.create(it, NSUTF8StringEncoding) }
                        continuation.resumeWithException(AuthorizationException("Revocation failed with code $statusCode: $errorBody"))
                    } else {
                        continuation.resume(Unit)
                    }
                }
            task.resume()
        }
    }

    private fun String.urlEncoded(): String {
        return this.replace("=", "%3D").replace("+", "%2B").replace("/", "%2F")
    }
}