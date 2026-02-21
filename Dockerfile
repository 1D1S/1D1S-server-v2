# Dockerfile.prod
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y tzdata tini \
    && rm -rf /var/lib/apt/lists/*
ENV TZ=Asia/Seoul

EXPOSE 8080

VOLUME /app/logs

ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/app/app.jar"]
