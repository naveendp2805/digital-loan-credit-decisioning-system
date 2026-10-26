FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY payment-service/pom.xml payment-service/pom.xml
COPY payment-service/src payment-service/src

RUN mvn -f payment-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/payment-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
