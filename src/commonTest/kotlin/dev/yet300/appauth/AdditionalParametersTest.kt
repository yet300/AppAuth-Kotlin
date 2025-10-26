package dev.yet300.appauth

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for additional OAuth/OIDC parameters
 * Covers various optional parameters that can be passed to authorization servers
 */
class AdditionalParametersTest {

    @Test
    fun testDisplayParameter() {
        // display parameter controls how auth UI is displayed
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("display" to "popup")
        )
        assertNotNull(request)
    }

    @Test
    fun testUILocalesParameter() {
        // ui_locales specifies preferred languages for the UI
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("ui_locales" to "en-US es-ES")
        )
        assertNotNull(request)
    }

    @Test
    fun testClaimsLocalesParameter() {
        // claims_locales specifies preferred languages for claims
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("claims_locales" to "en fr")
        )
        assertNotNull(request)
    }

    @Test
    fun testIdTokenHintParameter() {
        // id_token_hint can be used to pre-select an account
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("id_token_hint" to "previous-id-token")
        )
        assertNotNull(request)
    }

    @Test
    fun testLoginHintParameter() {
        // login_hint can pre-fill the username
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("login_hint" to "user@example.com")
        )
        assertNotNull(request)
    }

    @Test
    fun testAcrValuesParameter() {
        // acr_values specifies authentication context class references
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("acr_values" to "urn:mace:incommon:iap:silver")
        )
        assertNotNull(request)
    }

    @Test
    fun testClaimsParameter() {
        // claims parameter requests specific claims
        val claimsJson = """{"userinfo":{"email":{"essential":true}}}"""
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("claims" to claimsJson)
        )
        assertNotNull(request)
    }

    @Test
    fun testAccessTypeOffline() {
        // access_type=offline requests a refresh token (Google-specific)
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("access_type" to "offline")
        )
        assertNotNull(request)
    }

    @Test
    fun testIncludeGrantedScopes() {
        // include_granted_scopes for incremental authorization (Google-specific)
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("include_granted_scopes" to "true")
        )
        assertNotNull(request)
    }

    @Test
    fun testMultipleAdditionalParameters() {
        val params = mapOf(
            "prompt" to "consent",
            "access_type" to "offline",
            "ui_locales" to "en-US",
            "login_hint" to "user@example.com",
            "state" to "random-state-123",
            "nonce" to "random-nonce-456"
        )

        val request = createTestAuthorizationRequest(
            additionalParameters = params
        )
        assertNotNull(request)
    }

    @Test
    fun testResourceParameter() {
        // resource parameter for resource indicators (RFC 8707)
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("resource" to "https://api.example.com")
        )
        assertNotNull(request)
    }

    @Test
    fun testAudienceParameter() {
        // audience parameter for specifying token audience
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("audience" to "https://api.example.com")
        )
        assertNotNull(request)
    }

    @Test
    fun testResponseModeParameter() {
        // response_mode controls how response is returned
        val request = createTestAuthorizationRequest(
            additionalParameters = mapOf("response_mode" to "form_post")
        )
        assertNotNull(request)
    }

    @Test
    fun testCodeChallengeParameters() {
        // PKCE parameters
        val params = mapOf(
            "code_challenge" to "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            "code_challenge_method" to "S256"
        )

        val request = createTestAuthorizationRequest(
            additionalParameters = params
        )
        assertNotNull(request)
    }
}
