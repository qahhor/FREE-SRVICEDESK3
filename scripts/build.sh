#!/bin/bash

# Service Desk Platform - Production Build Script

set -e

echo "========================================="
echo "Service Desk Platform - Production Build"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
VERSION="0.1.0"
BUILD_DIR="build"
RELEASE_DIR="release"

# Functions
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${YELLOW}ℹ${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 is not installed. Please install it first."
        exit 1
    fi
}

# Check prerequisites
print_info "Checking prerequisites..."
check_command java
check_command mvn
check_command node
check_command npm
print_success "All prerequisites found"
echo ""

# Display versions
print_info "Tool versions:"
java -version 2>&1 | head -1
mvn -version | head -1
node --version
npm --version
echo ""

# Clean previous builds
print_info "Cleaning previous builds..."
rm -rf $BUILD_DIR $RELEASE_DIR
mkdir -p $BUILD_DIR $RELEASE_DIR
print_success "Clean completed"
echo ""

# Build backend
print_info "Building backend..."
cd backend
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    print_success "Backend build completed"
else
    print_error "Backend build failed"
    exit 1
fi
cd ..
echo ""

# Copy backend artifacts
print_info "Copying backend artifacts..."
mkdir -p $BUILD_DIR/backend
cp backend/ticket-service/target/ticket-service-*.jar $BUILD_DIR/backend/
print_success "Backend artifacts copied"
echo ""

# Build frontend
print_info "Building frontend..."
cd frontend/agent-app
npm install
npm run build --configuration production
if [ $? -eq 0 ]; then
    print_success "Frontend build completed"
else
    print_error "Frontend build failed"
    exit 1
fi
cd ../..
echo ""

# Copy frontend artifacts
print_info "Copying frontend artifacts..."
mkdir -p $BUILD_DIR/frontend
cp -r frontend/agent-app/dist/agent-app/* $BUILD_DIR/frontend/
print_success "Frontend artifacts copied"
echo ""

# Copy configuration files
print_info "Copying configuration files..."
cp .env.example $BUILD_DIR/
cp docker-compose.prod.yml $BUILD_DIR/
cp -r docs $BUILD_DIR/
cp -r infrastructure $BUILD_DIR/
cp README.md $BUILD_DIR/
cp LICENSE $BUILD_DIR/
print_success "Configuration files copied"
echo ""

# Create Docker images
print_info "Building Docker images..."
docker build -t servicedesk/ticket-service:$VERSION \
    -f backend/ticket-service/Dockerfile backend
if [ $? -eq 0 ]; then
    print_success "Docker images built"
else
    print_error "Docker build failed"
    exit 1
fi
echo ""

# Create release package
print_info "Creating release package..."
cd $BUILD_DIR
tar -czf ../$RELEASE_DIR/servicedesk-$VERSION.tar.gz .
cd ..
print_success "Release package created: $RELEASE_DIR/servicedesk-$VERSION.tar.gz"
echo ""

# Create checksums
print_info "Creating checksums..."
cd $RELEASE_DIR
sha256sum servicedesk-$VERSION.tar.gz > servicedesk-$VERSION.tar.gz.sha256
md5sum servicedesk-$VERSION.tar.gz > servicedesk-$VERSION.tar.gz.md5
cd ..
print_success "Checksums created"
echo ""

# Display results
echo "========================================="
echo "Build Summary"
echo "========================================="
echo "Version: $VERSION"
echo "Build directory: $BUILD_DIR"
echo "Release package: $RELEASE_DIR/servicedesk-$VERSION.tar.gz"
echo ""
echo "Artifacts:"
ls -lh $BUILD_DIR/backend/*.jar
echo ""
echo "Release package size:"
ls -lh $RELEASE_DIR/servicedesk-$VERSION.tar.gz
echo ""
print_success "Build completed successfully!"
echo ""
echo "Next steps:"
echo "1. Test the build: ./scripts/test-build.sh"
echo "2. Deploy: See docs/DEPLOYMENT.md"
echo ""
