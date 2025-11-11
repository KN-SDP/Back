FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

COPY src/main/resources/config/application-local.yml ./application-local.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:./application-local.yml", "app.jar"]