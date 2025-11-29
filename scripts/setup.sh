#!/bin/bash

# Service Desk Platform - Setup Script
# This script sets up the development environment

set -e

echo "🚀 Service Desk Platform - Setup"
echo "=================================="
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."

# Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo "✅ Java $JAVA_VERSION found"
    else
        echo "❌ Java 17+ is required. Current version: $JAVA_VERSION"
        exit 1
    fi
else
    echo "❌ Java is not installed. Please install Java 17+"
    exit 1
fi

# Maven
if command -v mvn &> /dev/null; then
    echo "✅ Maven found"
else
    echo "❌ Maven is not installed. Please install Maven 3.8+"
    exit 1
fi

# Docker
if command -v docker &> /dev/null; then
    echo "✅ Docker found"
else
    echo "❌ Docker is not installed. Please install Docker"
    exit 1
fi

# Docker Compose
if command -v docker-compose &> /dev/null; then
    echo "✅ Docker Compose found"
else
    echo "❌ Docker Compose is not installed. Please install Docker Compose"
    exit 1
fi

echo ""
echo "📦 Starting infrastructure services..."
docker-compose -f docker-compose.dev.yml up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

echo ""
echo "🏗️  Building backend..."
cd backend
mvn clean install -DskipTests

echo ""
echo "✅ Setup complete!"
echo ""
echo "📝 Next steps:"
echo "   1. cd backend/ticket-service"
echo "   2. mvn spring-boot:run"
echo "   3. Open http://localhost:8080/api/v1"
echo ""
echo "🔑 Default credentials:"
echo "   Email: admin@servicedesk.io"
echo "   Password: admin123"
echo ""
echo "📚 Documentation: https://github.com/qahhor/FREE-SRVICEDESK3"
