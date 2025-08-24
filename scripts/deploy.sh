#!/bin/bash

# AppAuth-Kotlin Deployment Script
# This script helps automate the deployment process to Maven Central

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required environment variables are set
check_environment() {
    print_status "Checking environment variables..."
    
    local missing_vars=()
    
    # Check for GitHub Packages credentials
    if [ -z "$GITHUB_TOKEN" ] && [ -z "$GPR_KEY" ]; then
        missing_vars+=("GITHUB_TOKEN or GPR_KEY")
    fi
    
    if [ -z "$GITHUB_ACTOR" ] && [ -z "$GPR_USER" ]; then
        missing_vars+=("GITHUB_ACTOR or GPR_USER")
    fi
    
    if [ ${#missing_vars[@]} -ne 0 ]; then
        print_error "Missing required environment variables:"
        for var in "${missing_vars[@]}"; do
            echo "  - $var"
        done
        echo ""
        echo "Please set these variables before running the deployment script."
        echo ""
        echo "For GitHub Packages, you need:"
        echo "  - GITHUB_TOKEN: Your GitHub personal access token"
        echo "  - GITHUB_ACTOR: Your GitHub username"
        exit 1
    fi
    
    print_status "Environment variables are properly configured."
}

# Get current version from gradle.properties
get_current_version() {
    grep "MODULE_VERSION_NUMBER=" gradle.properties | cut -d'=' -f2
}

# Update version in gradle.properties
update_version() {
    local new_version=$1
    sed -i "s/MODULE_VERSION_NUMBER=.*/MODULE_VERSION_NUMBER=$new_version/" gradle.properties
    print_status "Updated version to $new_version"
}

# Build and test the project
build_and_test() {
    print_status "Building and testing the project..."
    
    ./gradlew clean build
    ./gradlew test
    ./gradlew ktlintCheck
    
    print_status "Build and tests completed successfully."
}

# Deploy to GitHub Packages
deploy() {
    local version=$1
    local is_snapshot=$2
    
    print_status "Deploying version $version to GitHub Packages..."
    
    ./gradlew publishToGitHubPackagesRepository
    
    if [ "$is_snapshot" = true ]; then
        print_status "Snapshot version $version deployed successfully to GitHub Packages."
        print_warning "Snapshot versions are automatically cleaned up after 30 days."
    else
        print_status "Release version $version deployed successfully to GitHub Packages."
        print_status "The release is now available at: https://github.com/yet300/AppAuth-Kotlin/packages"
        print_status "Artifact: dev.yet300:appauth-kotlin:$version"
    fi
}

# Create git tag
create_tag() {
    local version=$1
    
    print_status "Creating git tag v$version..."
    git tag "v$version"
    git push origin "v$version"
    print_status "Tag v$version created and pushed."
}

# Main deployment function
main() {
    local version=$1
    local is_snapshot=${2:-false}
    
    if [ -z "$version" ]; then
        print_error "Usage: $0 <version> [snapshot]"
        echo "  version: The version to deploy (e.g., 0.1.3)"
        echo "  snapshot: Set to 'true' for snapshot deployment"
        exit 1
    fi
    
    print_status "Starting deployment process for version $version"
    
    # Check environment
    check_environment
    
    # Update version
    update_version "$version"
    
    # Build and test
    build_and_test
    
    # Deploy
    deploy "$version" "$is_snapshot"
    
    # Create tag for release versions
    if [ "$is_snapshot" != true ]; then
        create_tag "$version"
    fi
    
    print_status "Deployment process completed successfully!"
}

# Handle command line arguments
case "${1:-}" in
    "snapshot")
        current_version=$(get_current_version)
        snapshot_version="${current_version}-SNAPSHOT"
        main "$snapshot_version" true
        ;;
    "release")
        if [ -z "$2" ]; then
            print_error "Please provide a version number for release deployment."
            exit 1
        fi
        main "$2" false
        ;;
    *)
        if [ -z "$1" ]; then
            print_error "Usage: $0 <command> [version]"
            echo "Commands:"
            echo "  snapshot    - Deploy current version as snapshot"
            echo "  release <v> - Deploy specified version as release"
            echo ""
            echo "Examples:"
            echo "  $0 snapshot"
            echo "  $0 release 0.1.3"
            exit 1
        else
            main "$1" false
        fi
        ;;
esac
