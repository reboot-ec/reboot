FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/wrapper/ .mvn/wrapper/
COPY mvnw ./mvnw
COPY backend/identity-service/pom.xml backend/identity-service/pom.xml
COPY backend/identity-service/src/main/ backend/identity-service/src/main/

RUN chmod +x ./mvnw \
    && ./mvnw -B -f backend/identity-service/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup \
    && adduser -S -G appgroup -h /app appuser \
    && chown appuser:appgroup "$JAVA_HOME/lib/security/cacerts"

WORKDIR /app

COPY --from=build --chown=appuser:appgroup \
    /workspace/backend/identity-service/target/identity-service-1.0.0-SNAPSHOT.jar \
    /app/app.jar
COPY --chown=appuser:appgroup backend/identity-service/entrypoint.sh /app/entrypoint.sh

RUN chmod 0555 /app/entrypoint.sh

USER appuser

EXPOSE 8081

ENTRYPOINT ["/app/entrypoint.sh"]
