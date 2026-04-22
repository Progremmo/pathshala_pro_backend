# ==========================================
# PathshalaPro - Dockerfile (Render Deployment)
# Multi-stage build: Maven build → slim JRE runtime
# ==========================================

# ---------- Stage 1: Build ----------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (layer-cache dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build the fat JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the final JAR from the build stage
COPY --from=builder /app/target/pathshalapro-1.0.0.jar app.jar

# Switch to non-root user
USER appuser

# Expose the default Spring Boot port
EXPOSE 8080

# JVM flags tuned for Render free tier (~512 MB RAM)
# All secrets are injected via Render's Environment Variables — no .env file needed in Docker
ENV JAVA_OPTS="-Xmx400m -Xms128m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
