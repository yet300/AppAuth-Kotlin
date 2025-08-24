# Maven Central Deployment Guide

This guide explains how to deploy AppAuth-Kotlin to Maven Central.

## Prerequisites

1. **OSSRH Account**: You need an account on [OSSRH (OSS Repository Hosting)](https://oss.sonatype.org/)
2. **GPG Key**: A GPG key for signing artifacts
3. **Group ID**: The group ID `dev.gitlive` must be registered in your OSSRH account

## Setup Steps

### 1. OSSRH Account Setup

1. Go to [OSSRH](https://oss.sonatype.org/) and create an account
2. Create a new ticket to register your group ID `dev.gitlive`
3. Wait for approval (usually takes 1-2 business days)

### 2. GPG Key Setup

#### Option A: Generate a new GPG key
```bash
# Generate a new GPG key
gpg --gen-key

# Export the public key
gpg --armor --export your-email@example.com > public-key.asc

# Upload the public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys your-key-id
```

#### Option B: Use existing GPG key
```bash
# List existing keys
gpg --list-keys

# Export your public key
gpg --armor --export your-key-id > public-key.asc
```

### 3. Environment Variables

Set the following environment variables:

```bash
export OSSRH_USERNAME="your-ossrh-username"
export OSSRH_PASSWORD="your-ossrh-password"
export SIGNING_KEY_ID="your-gpg-key-id"
export SIGNING_PASSWORD="your-gpg-key-password"
export SIGNING_SECRET_KEY_RING_FILE="/path/to/your/secring.gpg"
```

### 4. Gradle Properties (Alternative)

Alternatively, you can set these in `gradle.properties`:

```properties
ossrhUsername=your-ossrh-username
ossrhPassword=your-ossrh-password
signing.keyId=your-gpg-key-id
signing.password=your-gpg-key-password
signing.secretKeyRingFile=/path/to/your/secring.gpg
```

## Deployment Process

### 1. Update Version

Update the version in `gradle.properties`:

```properties
MODULE_VERSION_NUMBER=0.1.3
```

### 2. Build and Test

```bash
# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Check for any issues
./gradlew ktlintCheck
```

### 3. Publish to Staging

```bash
# Publish to OSSRH staging repository
./gradlew publishToOSSRHRepository
```

### 4. Release Process

1. Go to [OSSRH Staging](https://oss.sonatype.org/#stagingRepositories)
2. Find your staging repository
3. Click "Close" to close the staging repository
4. Click "Release" to release to Maven Central

### 5. Verify Release

After release (can take up to 10 minutes):

```bash
# Check if the artifact is available
curl https://repo1.maven.org/maven2/dev/gitlive/appauth-kotlin/0.1.3/
```

## Snapshot Releases

For snapshot releases:

```bash
# Update version to include -SNAPSHOT
# In gradle.properties: MODULE_VERSION_NUMBER=0.1.3-SNAPSHOT

# Publish snapshot
./gradlew publishToOSSRHSnapshotRepository
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**: Check your OSSRH credentials
2. **Signing Failed**: Verify your GPG key configuration
3. **Group ID Not Registered**: Wait for OSSRH approval
4. **Version Already Exists**: Increment the version number

### Useful Commands

```bash
# Check GPG key
gpg --list-keys

# Test signing
echo "test" | gpg --clearsign

# Verify published artifacts
./gradlew publishToOSSRHRepository --dry-run
```

## CI/CD Integration

For automated deployment, you can use GitHub Actions. Create a workflow file:

```yaml
name: Deploy to Maven Central

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Setup GPG
        run: |
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | gpg --import
          echo "${{ secrets.GPG_PASSPHRASE }}" | gpg --batch --yes --passphrase-fd 0 --pinentry-mode loopback --export-secret-key ${{ secrets.GPG_KEY_ID }} > secring.gpg
      
      - name: Deploy to Maven Central
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          SIGNING_KEY_ID: ${{ secrets.GPG_KEY_ID }}
          SIGNING_PASSWORD: ${{ secrets.GPG_PASSPHRASE }}
          SIGNING_SECRET_KEY_RING_FILE: secring.gpg
        run: ./gradlew publishToOSSRHRepository
```

## Notes

- Maven Central has strict requirements for artifact metadata
- All artifacts must be signed with GPG
- The group ID must be registered and approved
- Release versions cannot be overwritten
- Snapshot versions are automatically cleaned up after 30 days


