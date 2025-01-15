FROM maven:3.9.7-amazoncorretto-17 AS builder
WORKDIR /opt/app
COPY . .

RUN ["mvn", "clean", "package", "dependency:copy-dependencies", "-Dmaven.test.skip", "-DincludeScope=runtime"]

FROM bellsoft/liberica-openjdk-debian:17
WORKDIR opt/app

COPY --from=builder /opt/app/target/reactive-video-1.0.jar .
COPY --from=builder /opt/app/target/classes/application-docker.yml application.yml
COPY --from=builder /opt/app/target/dependency ./libs

ENV SERVER_PORT=8081
EXPOSE 8081

CMD ["java", "-cp", "./libs/*:./reactive-video-1.0.jar", "ru.vasili4.reactive_video.ReactiveVideoApplication", "--spring.config.location=./application.yml"]
