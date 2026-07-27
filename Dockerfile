FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:9a1f7d4e7435d395d0da045115437479ce052cd5ff9cdaf2be92059f78c4c99d

ENV APP_NAME=familie-baks-soknad-api

COPY --chown=nonroot:nonroot ./target/familie-baks-soknad-api-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"

# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2", "-jar", "/app/app.jar" ]
