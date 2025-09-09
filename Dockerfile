# Use Maven to build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use a lightweight JDK 17 base image for running the app
FROM eclipse-temurin:17-jdk-alpine

# Install Tesseract OCR and dependencies
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng leptonica ghostscript poppler-utils

# Set working directory
WORKDIR /app

# Copy the jar from the builder stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for temporary files
RUN mkdir -p /app/temp

# Expose the port (default Spring Boot port or overridden via env)
EXPOSE 8003

# Run the application
ENTRYPOINT ["java", "-Dlogging.level.org.apache.http.wire=ERROR", "-jar", "app.jar"]
