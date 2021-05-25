FROM openjdk:12-alpine

EXPOSE 8080:8080
RUN apk add --no-cache bash

WORKDIR /ktor-sample

CMD ./gradlew run
