# Build stage
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy Maven wrapper files and make the wrapper executable
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/Hospital-Management-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
