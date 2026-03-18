# ============
# 🏗️ STAGE 1 - BUILD THE APP
# ============
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Maven runner
COPY mvnw .

COPY .mvn .mvn

# Dependency 
COPY pom.xml .

COPY settings.xml .

RUN ./mvnw dependency:go-offline -U

# Copy the full source code
COPY src .

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# ============
# 🚀 STAGE 2 - RUN THE APP
# ============
FROM eclipse-temurin:21-jre-alpine AS runner

ARG MY_MODULE

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/${MY_MODULE}_service/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
