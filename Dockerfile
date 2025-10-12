# ---- Stage 1: Build with Maven ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy only the pom.xml first (so dependency downloads can be cached)
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Now copy the rest of your project files
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# ---- Stage 2: Run the built JAR ----
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
