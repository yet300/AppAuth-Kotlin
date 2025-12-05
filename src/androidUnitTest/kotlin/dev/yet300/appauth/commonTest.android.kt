package dev.yet300.appauth

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import net.openid.appauth.AuthorizationServiceConfiguration
import org.robolectric.RuntimeEnvironment

actual val context: Any
    get() = RuntimeEnvironment.getApplication()

actual fun simulateSignIn() {
    // This would simulate a user sign-in action in a real test scenario
    // For now, it's a no-op as the actual sign-in requires UI interaction
}

actual suspend fun CoroutineScope.withAuthorizationService(action: suspend (AuthorizationService) -> Unit) {
    val androidContext = RuntimeEnvironment.getApplication() as Context
    val contextWrapper = ContextWrapper(androidContext)
    val service = AuthorizationService { contextWrapper }
    action(service)
}

/**
 * Sets up a mock OAuth server by mocking the AppAuth library's fetchFromIssuer method.
 * This is a higher-level mock that bypasses the network layer entirely.
 * Returns a mock URL (localhost-based) and a cleanup function.
 */
actual fun setupMockOAuthServer(originalUrl: String): Pair<String, () -> Unit> {
    // Use a mock server URL instead of the real URL
    // This makes it clear it's a test and avoids any accidental network calls
    val mockBaseUrl = "http://localhost:8080/auth/realms/MyRealm"
    // Ensure Robolectric RuntimeEnvironment is initialized first
    // This is critical for Uri.parse() to work in Android unit tests
    // Note: This function is only used by Android-specific tests that have @RunWith(RobolectricTestRunner::class)
    
    // Check if Robolectric is available by trying to get the application
    val robolectricAvailable = try {
        RuntimeEnvironment.getApplication()
        true
    } catch (e: Exception) {
        false
    }
    
    // If Robolectric isn't available, throw a catchable exception BEFORE trying to use Uri.parse()
    // This prevents the RuntimeException from Uri.parse() "not mocked" error
    if (!robolectricAvailable) {
        throw IllegalStateException(
            "SKIP_TEST_ON_ANDROID: Robolectric not initialized. " +
                "Use AuthorizationServiceTestAndroidCommon instead for Android tests."
        )
    }
    
    // Use mock URLs based on the mock base URL
    val authorizationEndpoint = "$mockBaseUrl/protocol/openid-connect/auth"
    val tokenEndpoint = "$mockBaseUrl/protocol/openid-connect/token"
    val endSessionEndpoint = "$mockBaseUrl/protocol/openid-connect/logout"
    
    // Create URIs - these work because Robolectric is initialized
    // Wrap in try-catch as a safety measure in case RuntimeEnvironment check passed but Uri.parse() still fails
    val authUri = try {
        Uri.parse(authorizationEndpoint)
    } catch (e: RuntimeException) {
        // If Uri.parse() fails, it means Robolectric isn't properly set up
        throw IllegalStateException(
            "SKIP_TEST_ON_ANDROID: Uri.parse() failed - Robolectric not properly initialized. " +
                "Use AuthorizationServiceTestAndroidCommon instead for Android tests.",
            e
        )
    }
    val tokenUri = Uri.parse(tokenEndpoint)
    val endSessionUri = Uri.parse(endSessionEndpoint)
    
    // Create a real AppAuth AuthorizationServiceConfiguration from the endpoints
    val appAuthConfig = net.openid.appauth.AuthorizationServiceConfiguration(
        authUri,
        tokenUri,
        null, // registrationEndpoint
        endSessionUri
    )
    
    // Mock the AppAuth library's static fetchFromIssuer method
    // This is a higher-level mock that bypasses the network layer entirely
    mockkStatic(AuthorizationServiceConfiguration::class)
    
    // Mock the static fetchFromIssuer to immediately invoke the callback with our configuration
    // Use slot to capture the callback argument
    val callbackSlot = slot<net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback>()
    
    every {
        AuthorizationServiceConfiguration.fetchFromIssuer(
            any<Uri>(),
            capture(callbackSlot)
        )
    } answers {
        // Invoke the captured callback immediately with our configuration
        // Since RetrieveConfigurationCallback is a SAM interface, we can invoke it as a function
        // The SAM interface has a single method that takes (config, exception)
        val callback = callbackSlot.captured
        // Use reflection to find and invoke the single abstract method
        val method = callback.javaClass.interfaces
            .flatMap { it.declaredMethods.toList() }
            .firstOrNull { it.parameterCount == 2 }
            ?: throw IllegalStateException("Could not find callback method")
        method.invoke(callback, appAuthConfig, null)
    }
    
    // Return the mock URL since we're using a mock server URL for testing
    // The cleanup function will unmock the static method
    return Pair(mockBaseUrl) {
        unmockkStatic(AuthorizationServiceConfiguration::class)
    }
}
