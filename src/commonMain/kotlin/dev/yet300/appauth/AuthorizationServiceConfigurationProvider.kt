package dev.yet300.appauth

/**
 * Abstraction over OIDC discovery ([AuthorizationServiceConfiguration.fetchFromIssuer]).
 *
 * Inject a custom provider in tests to avoid network calls while keeping production
 * code on the default discovery implementation.
 */
fun interface AuthorizationServiceConfigurationProvider {
    suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration

    companion object {
        /** Production provider that performs live OIDC discovery. */
        val Default: AuthorizationServiceConfigurationProvider =
            AuthorizationServiceConfigurationProvider { url ->
                AuthorizationServiceConfiguration.fetchFromIssuer(url)
            }
    }
}

/**
 * Returns service configuration using [provider], defaulting to live OIDC discovery.
 */
suspend fun fetchAuthorizationServiceConfiguration(
    issuerUrl: String,
    provider: AuthorizationServiceConfigurationProvider = AuthorizationServiceConfigurationProvider.Default,
): AuthorizationServiceConfiguration = provider.fetchFromIssuer(issuerUrl)

/**
 * Test helper that always returns a fixed configuration, ignoring the issuer URL.
 */
class StaticAuthorizationServiceConfigurationProvider(
    private val configuration: AuthorizationServiceConfiguration,
) : AuthorizationServiceConfigurationProvider {
    override suspend fun fetchFromIssuer(url: String): AuthorizationServiceConfiguration = configuration
}
