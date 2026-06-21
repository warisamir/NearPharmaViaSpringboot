FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
ARG JAR_FILE=target/NearPharma-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
# No secrets files copied — all config is injected via environment variables at runtime
ENTRYPOINT ["java", "-jar", "/app.jar"]
