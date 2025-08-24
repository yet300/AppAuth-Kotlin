package dev.yet300.appauth

import kotlinx.coroutines.CoroutineScope

actual val context: Any
    get() = "mock-context"

actual fun simulateSignIn() {
}

actual suspend fun CoroutineScope.withAuthorizationService(action: suspend (service: AuthorizationService) -> Unit) {
}
