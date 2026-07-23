FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:32ca8f96972f340f8e5aa97eb8e39f1864a1f9d0bae30f26a200ab02256999b4

ENV APP_NAME=familie-baks-soknad-api

COPY --chown=nonroot:nonroot ./target/familie-baks-soknad-api-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"

# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2", "-jar", "/app/app.jar" ]
