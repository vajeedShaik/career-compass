# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies separately, so Docker can
# cache this layer and skip re-downloading on every code change.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Runs as a non-root user for basic container security hygiene.
RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Render (and most hosts) inject PORT at runtime; application.yml reads it
# via ${PORT:8080}, so this EXPOSE is documentation, not a hard binding.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]