FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/NearPharma-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
COPY ./env.properties /config/env.properties
WORKDIR /
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=file:/config/env.properties"]

#FROM openjdk:17-jdk-slim
#
## Set a volume for the app
#VOLUME /tmp
#
## Copy the JAR file into the container
#ARG JAR_FILE=target/NearPharma-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} app.jar
#
## Copy the wait-for-it script
#COPY wait-for-it.sh /wait-for-it.sh
#RUN chmod +x /wait-for-it.sh
#
## Run the JAR file with wait-for-it to ensure the database is ready
#ENTRYPOINT ["./wait-for-it.sh", "postgres:5432", "--", "java", "-jar", "/app.jar"]
