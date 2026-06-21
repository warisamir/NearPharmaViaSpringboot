# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first
COPY pom.xml .

# Download dependencies (cached separately)
RUN mvn clean verify -q --fail-at-end || mvn help:active-profiles

# Copy all project files
COPY . .

# Build application - with verbose output
RUN mvn package -Dmaven.test.skip=true -e || { \
    echo "BUILD FAILED"; \
    find . -name "*.jar" -type f; \
    exit 1; \
  }

# List build artifacts for debugging
RUN echo "=== Build artifacts ===" && \
    ls -lah target/ && \
    ls -lah target/*.jar 2>/dev/null || echo "No JAR found in target/"

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/NearPharma-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
