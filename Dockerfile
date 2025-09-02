# Use a lightweight OpenJDK image
FROM eclipse-temurin:17-jdk-alpine

# Expose port 8080
EXPOSE 8080

# Add the jar file to the container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the app
ENTRYPOINT ["java","-jar","/app.jar"]
