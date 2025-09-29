# Use official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set working directory in container
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create directory for database
RUN mkdir -p /app/db

# Expose port 8080
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/connections-backend-1.0.0.jar"]