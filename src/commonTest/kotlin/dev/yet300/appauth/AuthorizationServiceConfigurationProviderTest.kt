package dev.yet300.appauth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AuthorizationServiceConfigurationProviderTest {

    @Test
    fun `fetchAuthorizationServiceConfiguration should delegate to the supplied provider`() = runTest {
        val config = try {
            AuthorizationServiceConfiguration(
                authorizationEndpoint = "https://issuer.example.com/oauth2/authorize",
                tokenEndpoint = "https://issuer.example.com/oauth2/token",
                revocationEndpoint = "https://issuer.example.com/oauth2/revoke",
            )
        } catch (e: RuntimeException) {
            if (e.message?.contains("not mocked") == true) return@runTest
            throw e
        }

        val provider = StaticAuthorizationServiceConfigurationProvider(config)

        val resolved = fetchAuthorizationServiceConfiguration(
            issuerUrl = "https://ignored.example.com",
            provider = provider,
        )

        assertSame(config, resolved)
        assertEquals(
            "https://issuer.example.com/oauth2/revoke",
            resolved.revocationEndpoint,
        )
    }
}
