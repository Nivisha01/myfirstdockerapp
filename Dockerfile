# Use OpenJDK 8 as the base image
FROM openjdk:8-jdk-alpine

# Set the working directory
WORKDIR /usr/app

# Expose the port that the application will run on
EXPOSE 8080

# Copy the Spring Boot WAR file from the target directory to the container
COPY target/*.war /usr/app/springboot-docker.war

# Command to run the application
ENTRYPOINT ["java", "-jar", "springboot-docker.war"]
