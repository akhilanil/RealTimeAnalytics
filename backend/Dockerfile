# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# First copy only pom.xml to cache dependencies
COPY pom.xml ./

# This downloads dependencies only for what's in pom.xml
RUN mvn -q -DskipTests package || true

# Now copy the source code
COPY src ./src

# Build app (this reuses cached deps from previous layer)
RUN mvn -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

RUN useradd -ms /bin/bash appuser
USER appuser

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=30s \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
    