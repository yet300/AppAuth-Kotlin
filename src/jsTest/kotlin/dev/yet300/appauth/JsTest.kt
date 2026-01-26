package dev.yet300.appauth

import kotlinx.coroutines.CoroutineScope

actual val context: Any
    get() = "mock-context"

actual fun simulateSignIn() {
}

actual suspend fun CoroutineScope.withAuthorizationService(action: suspend (service: AuthorizationService) -> Unit) {
}

actual fun setupMockOAuthServer(originalUrl: String): Pair<String, () -> Unit> {
    // JS platform doesn't support MockWebServer, return original URL
    return Pair(originalUrl) {}
}
