FROM eclipse-temurin:21-jre-alpine
LABEL authors="zhaomh"

WORKDIR /app

RUN mkdir -p /app/plugins

COPY ./build/libs/ChatBot-1.0.0-all.jar /app/app.jar
COPY ./plugins/ /app/plugins/

ENV HOST=0.0.0.0
ENV PORT=3001
ENV TOKEN=""

EXPOSE 8079 8078

ENTRYPOINT ["java","-Xmx1042m", "-Xms512m", "-jar", "/app/app.jar"]