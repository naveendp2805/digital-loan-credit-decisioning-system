FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY customer-service/pom.xml customer-service/pom.xml
COPY customer-service/src customer-service/src

RUN mvn -f customer-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/customer-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
