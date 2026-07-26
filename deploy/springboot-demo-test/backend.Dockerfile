FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre-alpine

RUN addgroup -S springboot && adduser -S -G springboot springboot

WORKDIR /app
COPY release/app.jar /app/app.jar

USER springboot:springboot
EXPOSE 18763

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
