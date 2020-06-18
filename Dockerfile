FROM navikt/java:11-appdynamics

ENV APPLICATION_NAME=familie-ba-soknad-api
ENV APPD_ENABLED=TRUE

COPY ./target/familie-ba-soknad-api-1.0-SNAPSHOT.jar "app.jar"
