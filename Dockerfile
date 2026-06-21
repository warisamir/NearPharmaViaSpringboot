# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use Maven + JDK to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and build JAR
COPY src ./src
RUN mvn package -Dmaven.test.skip=true -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Use lightweight JRE for running the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/NearPharma-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# All config is injected via environment variables at runtime
ENTRYPOINT ["java", "-jar", "/app.jar"]
