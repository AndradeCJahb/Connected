# Use official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set working directory in container
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy project files
COPY pom.xml .
COPY src ./src

# Expose port 8080
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application directly with Maven
CMD ["mvn", "clean", "compile", "exec:java"]