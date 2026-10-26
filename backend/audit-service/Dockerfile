FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY audit-service/pom.xml audit-service/pom.xml
COPY audit-service/src audit-service/src

RUN mvn -f audit-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/audit-service/target/*.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
