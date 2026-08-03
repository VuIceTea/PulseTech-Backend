FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
ARG SERVICE=api-gateway
COPY . .
RUN mvn -pl ${SERVICE} -am package -DskipTests && cp "${SERVICE}/target/${SERVICE}-1.0.0.jar" /service.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]