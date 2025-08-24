package dev.yet300.appauth

import kotlinx.coroutines.CoroutineScope

actual val context: Any
    get() = TODO("context not yet implemented")

actual fun simulateSignIn() {
}

actual suspend fun CoroutineScope.withAuthorizationService(action: suspend (service: AuthorizationService) -> Unit) {
}
