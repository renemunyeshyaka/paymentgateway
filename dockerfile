# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom and download dependencies in separate layer for caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage  
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the application jar
COPY --from=builder /app/target/*.jar app.jar

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]