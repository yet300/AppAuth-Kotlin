package dev.yet300.appauth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class TokenOperationsContractTest {

    private class HeadlessFake : TokenOperations {
        override suspend fun performTokenRequest(request: TokenRequest): TokenResponse {
            error("not stubbed")
        }

        override suspend fun performRevokeTokenRequest(request: RevokeTokenRequest) = Unit
    }

    @Test
    fun `TokenOperations can be faked independently of interactive authorization`() = runTest {
        assertNotNull(HeadlessFake())
    }
}
