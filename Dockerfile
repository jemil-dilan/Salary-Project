# syntax=docker/dockerfile:1.7

# =============================================================================
# Stage 1 - Build
# =============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build configuration first for better layer caching
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle* settings.gradle* gradle.properties* ./

# Ensure the Gradle wrapper is executable
RUN chmod +x gradlew

# Pre-download dependencies using Docker BuildKit cache
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# Copy application sources
COPY src/ src/

# Build the layered Spring Boot JAR and extract its layers
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar --no-daemon && \
    java -Djarmode=layertools \
         -jar build/libs/*.jar \
         extract \
         --destination extracted

# =============================================================================
# Stage 2 - Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-jammy

LABEL org.opencontainers.image.title="Spring Boot Application"
LABEL org.opencontainers.image.description="Production image"
LABEL org.opencontainers.image.vendor="Jemil"

# Create a dedicated non-root user
RUN groupadd --system spring && \
    useradd --system --gid spring --create-home spring

WORKDIR /app

# Copy Spring Boot layers in the optimal order
COPY --from=builder --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/application/ ./

USER spring

EXPOSE 8080

# JVM options can be overridden at runtime
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]