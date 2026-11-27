FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY src/main/ src/main/

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup \
    && adduser -S -G appgroup -h /app appuser \
    && chown appuser:appgroup "$JAVA_HOME/lib/security/cacerts"

WORKDIR /app

COPY --from=build --chown=appuser:appgroup \
    /workspace/target/organization-service-1.0.0-SNAPSHOT.jar \
    /app/app.jar

USER appuser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
