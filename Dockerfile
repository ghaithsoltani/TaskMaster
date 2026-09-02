# ============================================
# TASKMASTER BACKEND - MULTI-STAGE BUILD
# ============================================
# Stage 1: Build the application
# Stage 2: Run the application
# Multi-stage keeps final image small and secure

# ============================================
# STAGE 1: BUILD
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for layer caching)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application (skip tests for build stage — tests run in CI)
RUN ./mvnw clean package -DskipTests -B

# ============================================
# STAGE 2: RUNTIME
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="devops@taskmaster.com"
LABEL version="1.0.0"
LABEL description="TaskMaster Backend - Spring Boot"

# Create non-root user for security
RUN addgroup -S taskmaster && adduser -S taskmaster -G taskmaster

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R taskmaster:taskmaster /app

# Switch to non-root user
USER taskmaster

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]