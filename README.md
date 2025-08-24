# AppAuth-Kotlin

<h1 align="left">AppAuth-Kotlin<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/gitliveapp/AppAuth-Kotlin?style=flat-square"> <a href="https://git.live"><img src="https://img.shields.io/badge/collaborate-on%20gitlive-blueviolet?style=flat-square"></a></h1>
<img align="left" width="75px" src="https://avatars2.githubusercontent.com/u/42865805?s=200&v=4"> 
  <b>Built and maintained with 🧡 by <a href="https://git.live">GitLive</a></b><br/>
  <i>Real-time code collaboration inside any IDE</i><br/>
<br/>
<br/>
The AppAuth-Kotlin SDK is a Kotlin-first SDK for AppAuth. It's API is similar to the <a href="https://github.com/openid/AppAuth-Android">Open ID AppAuth Android</a> but also supports multiplatform projects, enabling you to use AppAuth directly from your common source targeting <strong>iOS</strong>, <strong>Android</strong> or <strong>JS</strong>.

## Installation

### Stable Release

To install simply add to your common sourceset in the build gradle:

```kotlin
implementation("dev.yet300:appauth-kotlin:0.1.2")
```

### Snapshot Release

For the latest development version:

```kotlin
implementation("dev.yet300:appauth-kotlin:0.1.3-SNAPSHOT")
```

### Repository Configuration

Since this is published to GitHub Packages, you may need to add the repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/yet300/AppAuth-Kotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

### Import Statement

Use the new package name in your imports:

```kotlin
import dev.yet300.appauth.*
```

Perform a gradle refresh and you should then be able to import the app auth files.

## Usage

```kotlin
val config = AuthorizationServiceConfiguration(
    authorizationEndpoint = "https://endpoint/oauth/authorize",
    tokenEndpoint = "https://endpoint/oauth/token",
    endSessionEndpoint = "https://endpoint/oauth/logout"
)
val request = AuthorizationRequest(
    config,
    "CLIENT_ID",
    listOf("openid", "profile", "member"),
    "code",
    "callback://oauth/callback"
)
try {
    val response = authorizationService.performAuthorizationRequest(request)
    tokenRequest.emit(response.createTokenExchangeRequest())
} catch (exception: AuthorizationException) {
    println("User attempted to cancel login")
}
```

## Development

### Building

```bash
./gradlew clean build
```

### Testing

```bash
./gradlew test
```

### Code Quality

```bash
./gradlew ktlintCheck
```

### Deployment

This project is configured for deployment to Maven Central. See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions.

#### Quick Deploy

1. Update version in `gradle.properties`
2. Set environment variables for OSSRH credentials and GPG signing
3. Run: `./gradlew publishToOSSRHRepository`
4. Complete the release process on [OSSRH](https://oss.sonatype.org/)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Run `./gradlew ktlintCheck` to ensure code quality
6. Submit a pull request
