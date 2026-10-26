FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY credit-decision-service/pom.xml credit-decision-service/pom.xml
COPY credit-decision-service/src credit-decision-service/src

RUN mvn -f credit-decision-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/credit-decision-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
