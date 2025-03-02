FROM oraclelinux:8-slim as builder

# Install Oracle JDK 21
RUN microdnf install -y java-21-openjdk && microdnf clean all

# Set the working directory
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/ResourceServer-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port

# Define the entry point command
ENTRYPOINT ["java","-jar", "app.jar"]