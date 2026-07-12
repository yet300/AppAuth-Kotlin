package dev.yet300.appauth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AuthorizationClientContractTest {
    private class FakeAuthorizationClient : AuthorizationClient {
        override suspend fun performAuthorizationRequest(request: AuthorizationRequest): AuthorizationResponse {
            error("not stubbed")
        }

        override suspend fun performEndSessionRequest(request: EndSessionRequest) {
            error("not stubbed")
        }

        override suspend fun performTokenRequest(request: TokenRequest): TokenResponse {
            error("not stubbed")
        }

        override suspend fun performRevokeTokenRequest(request: RevokeTokenRequest) = Unit
    }

    @Test
    fun `AuthorizationClient can be implemented in commonTest without a platform AuthorizationService`() =
        runTest {
            assertNotNull(FakeAuthorizationClient())
        }
}
