FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY audit-common/pom.xml audit-common/pom.xml
COPY audit-common/src audit-common/src

RUN mvn -f audit-common/pom.xml clean install -DskipTests

COPY repayment-service/pom.xml repayment-service/pom.xml
COPY repayment-service/src repayment-service/src

RUN mvn -f repayment-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/repayment-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
