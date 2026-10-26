FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY document-service/pom.xml document-service/pom.xml
COPY document-service/src document-service/src

RUN mvn -f document-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/document-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
