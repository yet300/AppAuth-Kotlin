package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for OAuth scope management
 * Scopes define the permissions requested from the user
 */
class ScopeManagementTest {

    @Test
    fun testOpenIdScope() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid")
        )
        assertNotNull(request)
    }

    @Test
    fun testProfileScope() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "profile")
        )
        assertNotNull(request)
    }

    @Test
    fun testEmailScope() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "email")
        )
        assertNotNull(request)
    }

    @Test
    fun testAddressScope() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "address")
        )
        assertNotNull(request)
    }

    @Test
    fun testPhoneScope() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "phone")
        )
        assertNotNull(request)
    }

    @Test
    fun testOfflineAccessScope() {
        // offline_access scope requests a refresh token
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "offline_access")
        )
        assertNotNull(request)
    }

    @Test
    fun testAllStandardScopes() {
        val request = createTestAuthorizationRequest(
            scopes = TestConstants.TEST_EXTENDED_SCOPES
        )
        assertNotNull(request)
    }

    @Test
    fun testCustomScopes() {
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "custom:read", "custom:write")
        )
        assertNotNull(request)
    }

    @Test
    fun testEmptyScopes() {
        val request = createTestAuthorizationRequest(
            scopes = emptyList()
        )
        assertNotNull(request)
    }

    @Test
    fun testSingleNonOpenIdScope() {
        // Some providers allow non-OIDC OAuth flows
        val request = createTestAuthorizationRequest(
            scopes = listOf("read:data")
        )
        assertNotNull(request)
    }

    @Test
    fun testDuplicateScopes() {
        // Test handling of duplicate scopes
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "profile", "profile", "email")
        )
        assertNotNull(request)
    }

    @Test
    fun testScopeWithSpaces() {
        // Scopes with spaces should be handled properly
        val request = createTestAuthorizationRequest(
            scopes = listOf("openid", "custom scope with spaces")
        )
        assertNotNull(request)
    }
}
