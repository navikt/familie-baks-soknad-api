FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:c303248322530595c12fe14a7624830b3d3eb8b63550bc0a7128ad1b3daed0eb

ENV APP_NAME=familie-baks-soknad-api

COPY --chown=nonroot:nonroot ./target/familie-baks-soknad-api-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"

# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2", "-jar", "/app/app.jar" ]
