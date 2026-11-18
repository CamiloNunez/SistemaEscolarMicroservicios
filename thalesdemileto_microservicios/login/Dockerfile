FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/login-microservice-1.0.0.jar app.jar

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.address=0.0.0.0 --server.port=${PORT:-10000}"]