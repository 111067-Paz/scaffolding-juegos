# ── Stage 1: build the jar with Maven (dependencies cached in their own layer) ──
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Tests run in CI / locally with `mvn verify`; the image build only packages.
RUN mvn -B -q package -DskipTests

# ── Stage 2: minimal runtime (JRE only, non-root user) ──
# Alternative: gcr.io/distroless/java21-debian12 (smaller, no shell) — but then the
# compose healthcheck can not use wget; you would need an HTTP check from outside.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
