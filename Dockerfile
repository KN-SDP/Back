FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

COPY src/main/resources/config/application-integration.yml ./application-integration.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:./application-integration.yml", "app.jar"]