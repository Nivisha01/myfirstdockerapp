# Use OpenJDK 8 as the base image
FROM openjdk:8-jdk-alpine

# Expose the port that the application will run on
EXPOSE 8080

# Copy the Spring Boot jar file from the target directory to the container
ADD target/springboot-docker.jar springboot-docker.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "/springboot-docker.jar"]
