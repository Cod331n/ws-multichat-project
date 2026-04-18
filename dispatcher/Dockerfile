FROM gradle:9-jdk25 AS build
WORKDIR /build

COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
COPY src src

RUN gradle clean build downloadLibs -x test

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /build/build/libs/*.jar /app/dispatcher.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

EXPOSE 8080
CMD ["java", "-jar", "dispatcher.jar"]