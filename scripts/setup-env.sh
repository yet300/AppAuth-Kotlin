#!/bin/bash

# Environment Setup Script for AppAuth-Kotlin Deployment
# This script helps you set up the required environment variables

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_help() {
    echo -e "${BLUE}[HELP]${NC} $1"
}

echo "=========================================="
echo "AppAuth-Kotlin Environment Setup"
echo "=========================================="
echo ""

print_info "This script will help you set up environment variables for Maven Central deployment."
echo ""

# Check if .env file exists
if [ -f ".env" ]; then
    print_warning "A .env file already exists. Do you want to overwrite it? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        print_info "Setup cancelled."
        exit 0
    fi
fi

echo "Please provide the following information:"
echo ""

# GitHub Packages Credentials
echo "=== GitHub Packages Credentials ==="
read -p "GitHub Username: " github_username
read -s -p "GitHub Personal Access Token: " github_token
echo ""

# Optional: GPG Signing (for future Maven Central deployment)
echo ""
print_help "GPG signing is optional for GitHub Packages but required for Maven Central:"
read -p "GPG Key ID (optional): " signing_key_id
if [ -n "$signing_key_id" ]; then
    read -s -p "GPG Key Password: " signing_password
    echo ""
fi

# Optional: Secret Key Ring File
if [ -n "$signing_key_id" ]; then
    echo ""
    print_help "If you have a GPG secret key ring file, provide the path (optional):"
    read -p "GPG Secret Key Ring File Path (optional): " signing_secret_key_ring_file
fi

# Create .env file
cat > .env << EOF
# GitHub Packages Credentials
export GITHUB_ACTOR="$github_username"
export GITHUB_TOKEN="$github_token"
export GPR_USER="$github_username"
export GPR_KEY="$github_token"
EOF

# Add GPG signing if provided
if [ -n "$signing_key_id" ]; then
    cat >> .env << EOF

# GPG Signing (optional)
export SIGNING_KEY_ID="$signing_key_id"
export SIGNING_PASSWORD="$signing_password"
EOF

    # Add secret key ring file if provided
    if [ -n "$signing_secret_key_ring_file" ]; then
        echo "export SIGNING_SECRET_KEY_RING_FILE=\"$signing_secret_key_ring_file\"" >> .env
    fi
fi

echo ""
print_info "Environment variables have been saved to .env file."
echo ""
print_help "To use these variables in your current shell, run:"
echo "  source .env"
echo ""
print_help "To use them in future sessions, add the following to your shell profile (.bashrc, .zshrc, etc.):"
echo "  source $(pwd)/.env"
echo ""
print_warning "Important: Keep your .env file secure and never commit it to version control!"
echo ""

# Add .env to .gitignore if not already there
if ! grep -q "^\.env$" .gitignore 2>/dev/null; then
    echo ".env" >> .gitignore
    print_info "Added .env to .gitignore"
fi

print_info "Environment setup completed!"
