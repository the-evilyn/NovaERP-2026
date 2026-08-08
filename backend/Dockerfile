# =============================================================================
# Stage 1: Build & Package NovaERP Backend
# =============================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper & POM to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# =============================================================================
# Stage 2: Production JRE Lightweight Runtime
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="NovaERP Engineering Team <engineering@novaerp.com>"
LABEL description="NovaERP Backend - Enterprise Core Service"

WORKDIR /app

# Create non-root system group and user for security hardening
RUN addgroup -S novaerp && adduser -S novaerp -G novaerp

# Copy fat JAR from builder stage
COPY --from=builder /build/target/*.jar /app/novaerp-backend.jar

# Set ownership to non-root user
RUN chown -R novaerp:novaerp /app

USER novaerp:novaerp

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE="docker"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/novaerp-backend.jar"]
