FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

COPY src/main/resources/config/application-prod.yml ./application-prod.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:./application-prod.yml", "app.jar"]
