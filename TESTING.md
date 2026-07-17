# Testing AppAuth-Kotlin

## Goal

Test **your wrapper code**, not the underlying AppAuth libraries (which are already tested by their maintainers).

## What to Test

Your Kotlin Multiplatform wrapper that bridges to native AppAuth SDKs:

1. **Parameter Mapping** - Verify Kotlin parameters are correctly passed to native SDKs
2. **Response Wrapping** - Verify native responses are correctly wrapped in Kotlin objects
3. **Coroutine Bridging** - Verify native callbacks are correctly bridged to suspend functions
4. **Error Handling** - Verify native errors are correctly wrapped in Kotlin exceptions

## What NOT to Test

- ❌ OAuth protocol correctness (AppAuth tests this)
- ❌ Network requests (AppAuth tests this)
- ❌ Token validation (AppAuth tests this)
- ❌ PKCE generation (AppAuth tests this)

## Test Approach

Use **instrumented tests** that run on real devices/simulators since you're wrapping native SDKs.

### Android Tests

**Location**: `src/androidInstrumentedTest/kotlin/dev/yet300/appauth/`

**Example**:
```kotlin
@RunWith(AndroidJUnit4::class)
class AuthorizationRequestWrapperTest {
    @Test
    fun testWrapperPassesParametersToNative() {
        val config = AuthorizationServiceConfiguration(
            authorizationEndpoint = "https://example.com/auth",
            tokenEndpoint = "https://example.com/token"
        )
        
        val request = AuthorizationRequest(
            config = config,
            clientId = "test-client",
            scopes = listOf("openid", "profile"),
            responseType = "code",
            redirectUri = "app:/callback",
            additionalParameters = mapOf("prompt" to "consent")
        )
        
        // Verify YOUR wrapper created native object correctly
        assertNotNull(request.android)
        assertEquals("test-client", request.android.clientId)
        assertEquals("consent", request.android.additionalParameters["prompt"])
    }
}
```

**Run**:
```bash
./gradlew connectedAndroidTest
```

### iOS Tests

**Location**: Native XCTest in Swift/Objective-C or `src/iosTest/`

**Example**:
```swift
class AuthorizationRequestWrapperTests: XCTestCase {
    func testWrapperPassesParametersToNative() {
        let config = AuthorizationServiceConfiguration(
            authorizationEndpoint: "https://example.com/auth",
            tokenEndpoint: "https://example.com/token"
        )
        
        let request = AuthorizationRequest(
            config: config,
            clientId: "test-client",
            scopes: ["openid", "profile"],
            responseType: "code",
            redirectUri: "app:/callback",
            additionalParameters: nil
        )
        
        // Verify YOUR wrapper created native object correctly
        XCTAssertNotNil(request.ios)
    }
}
```

**Run**:
```bash
xcodebuild test -scheme YourApp -destination 'platform=iOS Simulator,name=iPhone 17'
```

## Test Coverage

Your wrapper tests should verify:

### 1. Configuration Wrapper
- ✅ Endpoints are correctly mapped
- ✅ Optional endpoints (registration, endSession, revocation) handled correctly
- ✅ Discovery (fetchFromIssuer) works

### 2. Request Wrappers
- ✅ AuthorizationRequest passes all parameters to native SDK
- ✅ TokenRequest handles different grant types
- ✅ EndSessionRequest passes logout parameters
- ✅ RevokeTokenRequest handles public/confidential clients
- ✅ Additional parameters are passed through

### 3. Response Wrappers
- ✅ AuthorizationResponse extracts authorization code
- ✅ TokenResponse extracts access/refresh/ID tokens
- ✅ Errors are correctly wrapped

### 4. Service Methods
- ✅ performAuthorizationRequest bridges callback to coroutine
- ✅ performTokenRequest bridges callback to coroutine
- ✅ performEndSessionRequest bridges callback to coroutine
- ✅ performRevokeTokenRequest works correctly

## Example Test File

See `src/androidInstrumentedTest/kotlin/dev/yet300/appauth/WrapperTests.kt` for complete examples.

## Running Tests

### Android
```bash
# All instrumented tests
./gradlew connectedAndroidTest

# Specific test class
./gradlew connectedAndroidTest --tests "*.AuthorizationRequestWrapperTest"

# Debug variant
./gradlew connectedDebugAndroidTest
```

### iOS
```bash
# From Xcode: Cmd+U

# From command line
xcodebuild test -scheme YourApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

## CI/CD

Add to your GitHub Actions:

```yaml
- name: Run Android Tests
  run: ./gradlew connectedAndroidTest
  
- name: Run iOS Tests
  run: xcodebuild test -scheme YourApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Summary

**Test your wrapper code** that bridges Kotlin to native AppAuth SDKs. Focus on:
- Parameter mapping
- Response wrapping
- Coroutine bridging
- Error handling
