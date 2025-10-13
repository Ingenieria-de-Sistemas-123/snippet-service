FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=docker"]
