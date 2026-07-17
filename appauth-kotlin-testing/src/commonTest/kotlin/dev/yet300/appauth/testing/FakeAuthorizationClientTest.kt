package dev.yet300.appauth.testing

import kotlin.test.Test
import kotlin.test.assertEquals

class FakeAuthorizationClientTest {

    @Test
    fun `FakeAuthorizationClient should start with empty call logs`() {
        val fake = FakeAuthorizationClient()

        assertEquals(0, fake.authorizationCalls.size)
        assertEquals(0, fake.endSessionCalls.size)
        assertEquals(0, fake.tokenCalls.size)
        assertEquals(0, fake.revokeCalls.size)
    }
}
