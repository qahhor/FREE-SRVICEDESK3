# Multi-stage Dockerfile for production-ready build

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy parent pom and common-lib first for better caching
COPY pom.xml .
COPY common-lib ./common-lib

# Copy ticket-service
COPY ticket-service ./ticket-service

# Build the application (skip tests for faster builds, run tests in CI/CD)
RUN mvn clean package -pl ticket-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Add labels
LABEL maintainer="Green White Solutions <support@greenwhite.uz>"
LABEL description="Service Desk Platform - Ticket Service"
LABEL version="0.1.0"

# Create non-root user
RUN addgroup -S servicedesk && adduser -S servicedesk -G servicedesk

# Set working directory
WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/ticket-service/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/servicedesk && \
    chown -R servicedesk:servicedesk /app /var/log/servicedesk

# Switch to non-root user
USER servicedesk

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod -jar app.jar
