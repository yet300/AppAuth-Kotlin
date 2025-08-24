package dev.yet300.appauth

import kotlinx.coroutines.CoroutineScope

actual val context: Any
    get() = TODO("Not yet implemented")

actual fun simulateSignIn() {
    TODO("Not yet implemented")
}

actual suspend fun CoroutineScope.withAuthorizationService(action: suspend (AuthorizationService) -> Unit) {
    TODO("Not yet implemented")
}