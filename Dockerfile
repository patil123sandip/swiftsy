# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/swiftsy-app-0.0.1-SNAPSHOT.jar app.jar
# Optimize memory for Render's free tier (512MB)
ENTRYPOINT ["java", "-Xmx384m", "-Xms384m", "-jar", "/app.jar"]
