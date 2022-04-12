# familie-ba-soknad-api

API for søknadsdialog for barnetrygd

## Kjøring lokalt

Applikasjonen kan startes lokalt fra filen `LokalLauncher`. Serveren blir da tilgjengelig på `localhost:8080`. Applikasjonen er
avhengig av to tjenester: familie-ba-mottak og pdl-api. Lokalt er disse mocket ut og mockene er knyttet til
spring-profilene `mock-mottak` og `mock-pdl`. Man kan velge å kjøre appen sammen med familie-ba-mottak og familie-mock-server,
dette gjøres ved å fjerne disse spring-profilene fra `LokalLauncher`. familie-mock-server fungerer som en ekstern mock for
pdl-api.

For å kjøre mot familie-ba-mottak lokalt må man gjøre noe endringer i `application-lokal.yaml`. <br>
Sette ```TOKEN_X_WELL_KNOWN_URL``` til ```https://fakedings.dev-gcp.nais.io/default/.well-known/openid-configuration``` <br>
og bytte ut `token-endpoint-url: http://metadata` med `grant-type: client_credentials`

I tillegg må man enten hente ut et nytt JWT-token ved å logge inn på poden til mottak eller sette @Unprotected på endepunkt i
mottak.

## Bygging

Appen bygges ved hjelp av maven. Den bruker pakker både fra Maven Central og Github Package Registry. For å hente pakkene fra
Github Package Registry kan man eksempelvis bruke følgende settings.xml:

```$xslt
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">  
  <servers>
    <server>
      <id>github</id>
      <username>navikt</username>
      <password>[personal access token]</password>
    </server>
  </servers>
</settings>
```

Her må man legge inn sitt personal access token fra github i password-feltet. Tokenet må ha SSO mot navikt enablet og må minst ha
tilgang til `read:packages`.

## Deploy

Applikasjonen kjører i clusteret `dev-gcp`. Deploy gjøres via Github Actions, der det er satt opp to ulike workflows. Den ene
workflowen kjører for brancher med en åpen pull request og inkluderer sonar-analyse. Den andre kjører ved push til master.

## Kodestil

Du må bruke prosjektets kodestil for å få deployet koden. Denne skal kjøre automatisk som git-hook, men kan også kjøres manuelt

```shell
mvn antrun:run@ktlint-format
```

## Kontaktinformasjon

For NAV-interne kan henvendelser rettes til #team-familie på slack. Ellers kan man opprette et issue her på github.
