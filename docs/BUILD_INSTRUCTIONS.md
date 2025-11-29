# Service Desk Platform - Build Instructions

## Prerequisites

- **Java JDK 17+** installed
- **Maven 3.8+** installed
- **Node.js 18+** and npm installed (for frontend)
- Internet connection (for downloading dependencies)

## Quick Build

```bash
# Clone repository
git clone https://github.com/qahhor/FREE-SRVICEDESK3.git
cd FREE-SRVICEDESK3

# Run build script
./scripts/build.sh
```

## Manual Build Steps

### 1. Build Backend

```bash
cd backend

# Clean and build all modules
mvn clean install

# Skip tests for faster build
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl ticket-service -am

# Build with tests
mvn clean verify
```

**Output**: JAR files in `target/` directories
- `common-lib/target/common-lib-0.1.0-SNAPSHOT.jar`
- `ticket-service/target/ticket-service-0.1.0-SNAPSHOT.jar`

### 2. Build Frontend

```bash
cd frontend/agent-app

# Install dependencies
npm install

# Development build
npm run build

# Production build
npm run build --configuration production
```

**Output**: Static files in `dist/agent-app/`

### 3. Run Tests

#### Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run tests for specific module
mvn test -pl ticket-service

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open ticket-service/target/site/jacoco/index.html
```

#### Frontend Tests

```bash
cd frontend/agent-app

# Run unit tests
npm test

# Run with coverage
npm test -- --coverage

# Run e2e tests (when implemented)
npm run e2e
```

### 4. Build Docker Images

```bash
# Build ticket-service image
docker build -t servicedesk/ticket-service:0.1.0 \
  -f backend/ticket-service/Dockerfile \
  backend

# Build frontend image (create Dockerfile first)
docker build -t servicedesk/agent-app:0.1.0 \
  -f frontend/agent-app/Dockerfile \
  frontend/agent-app

# Build all with Docker Compose
docker-compose -f docker-compose.prod.yml build
```

## Production Build

### Step 1: Version Update

Update version in:
- `backend/pom.xml`
- `frontend/agent-app/package.json`
- `docker-compose.prod.yml`

```bash
# Example: Update to 0.2.0
sed -i 's/0.1.0-SNAPSHOT/0.2.0/g' backend/pom.xml
sed -i 's/0.1.0/0.2.0/g' frontend/agent-app/package.json
```

### Step 2: Clean Build

```bash
# Backend
cd backend
mvn clean verify -P production

# Frontend
cd ../frontend/agent-app
rm -rf node_modules dist
npm install
npm run build --configuration production
```

### Step 3: Create Release Artifacts

```bash
# Create release directory
mkdir -p release/servicedesk-0.1.0

# Copy backend artifacts
cp backend/ticket-service/target/ticket-service-0.1.0-SNAPSHOT.jar \
   release/servicedesk-0.1.0/

# Copy frontend artifacts
cp -r frontend/agent-app/dist/agent-app \
   release/servicedesk-0.1.0/frontend

# Copy configuration
cp .env.example release/servicedesk-0.1.0/
cp docker-compose.prod.yml release/servicedesk-0.1.0/
cp -r docs release/servicedesk-0.1.0/

# Create tarball
cd release
tar -czf servicedesk-0.1.0.tar.gz servicedesk-0.1.0/

# Create checksums
sha256sum servicedesk-0.1.0.tar.gz > servicedesk-0.1.0.tar.gz.sha256
```

## CI/CD Build (GitHub Actions)

The project uses GitHub Actions for automated builds. See `.github/workflows/build.yml`

### Triggers

- **Push** to `main` or `develop` branches
- **Pull Request** to `main`
- **Tag** matching `v*` pattern

### Build Steps

1. Checkout code
2. Setup Java 17
3. Setup Node.js 18
4. Cache Maven dependencies
5. Cache npm dependencies
6. Run backend tests
7. Run frontend tests
8. Build backend JAR
9. Build frontend dist
10. Build Docker images
11. Push to registry (on tag)
12. Create GitHub release (on tag)

## Build Optimization

### Maven Build Speed

```bash
# Use parallel builds
mvn clean install -T 1C

# Skip tests
mvn clean install -DskipTests

# Offline mode (if dependencies cached)
mvn clean install -o

# Update snapshots
mvn clean install -U
```

### Frontend Build Speed

```bash
# Use npm ci for clean installs
npm ci

# Enable caching
npm install --prefer-offline

# Use build cache
npm run build -- --build-optimizer
```

## Troubleshooting

### "Could not resolve dependencies"

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Re-download dependencies
mvn clean install -U
```

### "Out of memory" during build

```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m"

# Increase Node memory
export NODE_OPTIONS="--max-old-space-size=4096"
```

### Test failures

```bash
# Skip failing tests temporarily
mvn install -DskipTests

# Run specific test
mvn test -Dtest=TicketServiceTest

# Debug test
mvn test -Dtest=TicketServiceTest -X
```

## Artifact Locations

After successful build:

```
backend/
├── common-lib/
│   └── target/
│       └── common-lib-0.1.0-SNAPSHOT.jar
└── ticket-service/
    └── target/
        ├── ticket-service-0.1.0-SNAPSHOT.jar (executable JAR)
        ├── classes/ (compiled classes)
        └── test-classes/ (test classes)

frontend/
└── agent-app/
    └── dist/
        └── agent-app/ (static files)
            ├── index.html
            ├── main.*.js
            ├── polyfills.*.js
            └── styles.*.css
```

## Running Built Artifacts

### Backend JAR

```bash
java -jar backend/ticket-service/target/ticket-service-0.1.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_PASSWORD=secret \
  --JWT_SECRET=your-secret-key
```

### Frontend Files

```bash
# Using http-server
npm install -g http-server
cd frontend/agent-app/dist/agent-app
http-server -p 4200

# Using nginx
cp -r frontend/agent-app/dist/agent-app/* /usr/share/nginx/html/
nginx -s reload
```

## Continuous Deployment

### Docker Registry

```bash
# Tag image
docker tag servicedesk/ticket-service:0.1.0 \
  registry.example.com/servicedesk/ticket-service:0.1.0

# Push to registry
docker push registry.example.com/servicedesk/ticket-service:0.1.0
```

### Kubernetes

```bash
# Update deployment
kubectl set image deployment/ticket-service \
  ticket-service=registry.example.com/servicedesk/ticket-service:0.1.0 \
  -n servicedesk

# Rollout status
kubectl rollout status deployment/ticket-service -n servicedesk
```

## Build Verification

### Smoke Tests

```bash
# Start services
docker-compose -f docker-compose.prod.yml up -d

# Wait for startup
sleep 30

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test login endpoint
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@servicedesk.io","password":"admin123"}'

# Test frontend
curl http://localhost:4200
```

## Performance Profiling

### JVM Profiling

```bash
# Enable JFR (Java Flight Recorder)
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
  -jar ticket-service.jar

# Analyze with JMC
jmc recording.jfr
```

### Frontend Profiling

```bash
# Build with source maps
npm run build -- --source-map

# Analyze bundle
npm run build -- --stats-json
npx webpack-bundle-analyzer dist/agent-app/stats.json
```

---

## References

- [Maven Documentation](https://maven.apache.org/guides/)
- [Spring Boot Build Tools](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
- [Angular Build](https://angular.io/guide/build)
- [Docker Build Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

**Last Updated**: January 2024
