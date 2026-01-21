FROM gradle:8-jdk AS build
WORKDIR /app
COPY . .
RUN gradle unitTest --no-daemon --console=plain --info
RUN gradle bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
