# Base image: OpenJDK 21
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# The name of the JAR file should be adjusted to your project
COPY build/libs/*-SNAPSHOT.jar app.jar

# Copy the prod profile YML file into the container
# NOTE: The path is updated to include the 'config' submodule folder.
COPY src/main/resources/config/application-prod.yml ./application-prod.yml

# Expose the port on which the application will run
EXPOSE 8080

# The command to run the application with prod profile using the external yml
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:./application-prod.yml", "app.jar"]
