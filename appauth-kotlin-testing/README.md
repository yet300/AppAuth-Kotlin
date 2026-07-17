## Testing

For unit tests, add the testing artifact to `commonTest`:

```kotlin
commonTest.dependencies {
    implementation("io.github.yet300:appauth-kotlin-testing:0.1.2")
}
```

Use [FakeAuthorizationClient](appauth-kotlin-testing/src/commonMain/kotlin/dev/yet300/appauth/testing/FakeAuthorizationClient.kt) to stub OAuth operations without a platform `AuthorizationService`:

```kotlin
val oauth = FakeAuthorizationClient()
oauth.onTokenRequest = { request -> /* return TokenResponse */ }
oauth.onRevokeTokenRequest = { /* record revocation */ }
```

Depends on [AuthorizationClient](src/commonMain/kotlin/dev/yet300/appauth/AuthorizationClient.kt) (see PR #6).
