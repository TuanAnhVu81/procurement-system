# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/procurement-system-0.0.1-SNAPSHOT.jar app.jar
# Create an empty .env file just in case spring-dotenv complains about missing file
RUN touch .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
