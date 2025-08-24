# Maven Central Deployment Setup Summary

This document summarizes all the changes made to prepare AppAuth-Kotlin for deployment to Maven Central.

## Changes Made

### 1. Build Configuration Updates

#### `build.gradle.kts`
- **Publishing Configuration**: Updated to target Maven Central (OSSRH) instead of GitHub Packages
- **Repository URLs**: 
  - Staging: `https://oss.sonatype.org/service/local/staging/deploy/maven2/`
  - Snapshots: `https://oss.sonatype.org/content/repositories/snapshots/`
- **Signing Configuration**: Enhanced to support both in-memory GPG keys and GPG command line
- **Credentials**: Updated to use OSSRH credentials instead of GitHub Packages

#### `gradle.properties`
- **Cleaned up configuration**: Removed GitHub Packages specific settings
- **Added comments**: Better organization and documentation
- **Signing configuration**: Prepared for GPG signing setup
- **Updated description**: More accurate project description

### 2. Documentation

#### `DEPLOYMENT.md`
- **Comprehensive deployment guide**: Step-by-step instructions for Maven Central deployment
- **Prerequisites**: OSSRH account, GPG key, group ID registration
- **Setup instructions**: Detailed setup process for all requirements
- **Deployment process**: Complete workflow from build to release
- **Troubleshooting**: Common issues and solutions
- **CI/CD integration**: GitHub Actions workflow examples

#### `README.md`
- **Updated installation instructions**: Added both stable and snapshot versions
- **Development section**: Added build, test, and deployment instructions
- **Contributing guidelines**: Clear contribution process
- **Fixed typo**: "Useage" → "Usage"

### 3. Automation Scripts

#### `scripts/deploy.sh`
- **Deployment automation**: Automated deployment process
- **Environment validation**: Checks for required environment variables
- **Version management**: Automatic version updates
- **Build and test**: Integrated build and test process
- **Git tagging**: Automatic tag creation for releases
- **Support for snapshots**: Handles both release and snapshot deployments

#### `scripts/setup-env.sh`
- **Environment setup**: Interactive setup for deployment credentials
- **Security**: Creates .env file and adds to .gitignore
- **User-friendly**: Guided setup process with clear instructions

### 4. CI/CD Integration

#### `.github/workflows/deploy.yml`
- **Release deployment**: Automated deployment on version tags
- **GPG setup**: Secure GPG key handling in CI
- **Build and test**: Comprehensive testing before deployment
- **Release creation**: Automatic GitHub release creation

#### `.github/workflows/deploy-snapshot.yml`
- **Snapshot deployment**: Automated snapshot releases on main/develop branches
- **Version management**: Automatic snapshot version creation
- **Continuous integration**: Regular snapshot updates

### 5. Security and Configuration

#### `.gitignore`
- **Environment variables**: Added .env to prevent credential exposure
- **Security**: Ensures sensitive data is not committed

## Prerequisites for Deployment

### 1. OSSRH Account
- Create account at [OSSRH](https://oss.sonatype.org/)
- Register group ID `dev.yet300`
- Wait for approval (1-2 business days)

### 2. GPG Key
- Generate or use existing GPG key
- Export public key to key servers
- Configure signing in environment

### 3. Environment Variables
Required environment variables:
- `OSSRH_USERNAME`: Your OSSRH username
- `OSSRH_PASSWORD`: Your OSSRH password
- `SIGNING_KEY_ID`: Your GPG key ID
- `SIGNING_PASSWORD`: Your GPG key password
- `SIGNING_SECRET_KEY_RING_FILE`: Path to GPG secret key ring (optional)

## Deployment Commands

### Manual Deployment
```bash
# Setup environment
./scripts/setup-env.sh

# Deploy snapshot
./scripts/deploy.sh snapshot

# Deploy release
./scripts/deploy.sh release 0.1.3
```

### Automated Deployment
```bash
# Create and push a tag for release
git tag v0.1.3
git push origin v0.1.3

# Push to main/develop for snapshot
git push origin main
```

## GitHub Secrets Required

For automated deployment, set these secrets in your GitHub repository:

- `OSSRH_USERNAME`: Your OSSRH username
- `OSSRH_PASSWORD`: Your OSSRH password
- `GPG_KEY_ID`: Your GPG key ID
- `GPG_PASSPHRASE`: Your GPG key passphrase
- `GPG_PRIVATE_KEY`: Your GPG private key (armored)

## Next Steps

1. **Register with OSSRH**: Create account and register group ID
2. **Set up GPG key**: Generate or configure existing key
3. **Configure environment**: Run setup script or set environment variables
4. **Test deployment**: Try snapshot deployment first
5. **Release**: Deploy first stable release

## Verification

After deployment, verify your artifacts are available:

```bash
# Check Maven Central
curl https://repo1.maven.org/maven2/dev/yet300/appauth-kotlin/0.1.3/

# Check OSSRH snapshots
curl https://oss.sonatype.org/content/repositories/snapshots/dev/yet300/appauth-kotlin/
```

## Notes

- All artifacts are automatically signed with GPG
- Release versions cannot be overwritten
- Snapshot versions are cleaned up after 30 days
- The group ID `dev.yet300` must be registered and approved
- Maven Central has strict metadata requirements that are now satisfied
