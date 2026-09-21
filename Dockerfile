FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:7dcec2bc8f99be5bed1d016c093fd9d19578be540cd633094531f4a169719a44

ENV APP_NAME=familie-baks-soknad-api

COPY --chown=nonroot:nonroot ./target/familie-baks-soknad-api-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"

# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2", "-jar", "/app/app.jar" ]
