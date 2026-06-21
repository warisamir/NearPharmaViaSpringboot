# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use Maven + JDK to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code and build JAR
COPY src ./src
RUN ./mvnw package -Dmaven.test.skip=true

# Verify JAR was created
RUN ls -lh /app/target/*.jar || (echo "ERROR: JAR build failed" && exit 1)

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
