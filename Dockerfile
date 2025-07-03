FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/NearPharma-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
COPY ./env.properties /config/env.properties
WORKDIR /
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=file:/config/env.properties"]
