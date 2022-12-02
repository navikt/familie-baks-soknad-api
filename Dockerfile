FROM ghcr.io/navikt/baseimages/temurin:17-appdynamics

ENV APPLICATION_NAME=familie-baks-soknad-api
ENV APPD_ENABLED=TRUE
ENV JAVA_OPTS="-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2"

COPY ./target/familie-baks-soknad-api-1.0-SNAPSHOT.jar "app.jar"
