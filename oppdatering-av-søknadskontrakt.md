# Oppdatering av søknad for BAKS

Ved innsendelse av søknad for barnetrygd eller kontantstøtte, krever vi at innsendelsen følger et forhåndsdefinert
format, som vi kaller `søknadskontrakten`. Søknadskontraktene til barnetrygd og kontantstøtte
forvaltes gjennom repoet `familie-kontrakter`. Her er det egne moduler for hver av ytelsene, og i hver modul ligger egne
mapper per versjon av søknadskontrakten for den enkelte yteslen. Grunnen til at vi har valgt å versjonere
søknadskontrakene er for å kunne være sikre på hva slags informasjon vi til enhver tid kan forvente at ligger i
søknaden.

## Serialisering og deserialisering av søknadskontrakten

For å enklere kunne håndtere ulike versjoner av søknadskontrakten har vi definert en `sealed class` som "wrapper"
søknadskontrakten i en felles klasse uavhengig av versjon. `VersjonertBarnetrygdSøknad` og
`VersjonertKontantstøtteSøknad`. For hver versjon av søknadskontrakten er det opprettet en `data class` som arver fra
`VersjonertBarnetrygdSøknad`/`VersjonertKontantstøtteSøknad`. Eksempelvis `VersjonertBarnetrygdSøknadV10` og
`VersjonertKontantstøtteSøknadV6`. Vi har også definert en egen `sealed class` `StøttetVersjonertBarnetrygdSøknad`/
`StøttetVersjonertKontantstøtteSøknad` som arver fra `VersjonertBarnetrygdSøknad`/`VersjonertKontantstøtteSøknad`, og
inneholder alle versjonene av søknadskontrakten som vi støtter i BAKS. Tanken
er man stort sett skal bruke `StøttetVersjonertBarnetrygdSøknad`/`StøttetVersjonertKontantstøtteSøknad` når man jobber
med søknaden i kode, og at man da bruker `when`
når man skal gå inn i detaljene til en bestemt versjon.

For at utveksling av klassen `VersjonertBarnetrygdSøknad`/`VersjonertKontantstøtteSøknad` skal fungere mellom apper
er det opprettet egendefinert serialisering og deserialisering av denne. Klassen er igjen annotert med `@JsonSerialize`
og `@JsonDeserialize` for å spesifisere at det er den egendefinserte serialiseringen og deserialiseringen som skal
benyttes.

## Opprette ny versjon av søknadskontrakten

Når det eksempelvis skal legges til et nytt spørsmål eller vi ønsker å endre på strukturen i søknadskontrakten, må det
opprettes en ny versjon av søknadskontrakten. Dette gjør man ved å opprette en ny mappe under ytelsen med navn lik den
nye
versjonen, eksempelvis `v11` for barnetrygd og `v7` for kontantstøtte. Der må man opprette en ny `data class` med navnet
`BarnetrygdSøknad`/`KontantstøtteSøknad` som arver fra `interfacet` `BaSøknadBase`/`KsSøknadBase`. Vanligvis vil man her
kopiere
tilsvarende `data class` fra forrige versjon først, også vil man modifisere på eventuelt nye felter eller legge til nye
versjoner av sub-typer. Dersom endringen i den nye versjonen av søknadskontrakten eksempelvis gjelder et nytt spørsmål
på barnet,
vil man i samme fil opprette en ny `data class` for `Barn` og gjøre nødvendige justeringer der i forhold til forrige
versjon av `Barn`. Se for eksempel på disse versjonene
av [BA](https://github.com/navikt/familie-kontrakter/blob/main/barnetrygd/src/main/kotlin/no/nav/familie/kontrakter/ba/s%C3%B8knad/v10/BarnetrygdS%C3%B8knad.kt)
og [KS](https://github.com/navikt/familie-kontrakter/blob/main/kontantstotte/src/main/kotlin/no/nav/familie/kontrakter/ks/s%C3%B8knad/v6/Kontantst%C3%B8tteS%C3%B8knad.kt)
søknadene.

Når den nye versjonen av søknadskontrakten er opprettet, må det opprettes en ny `data class`
`VersjonertBarnetrygdSøknadV?`/`VersjonertKontantstøtteSøknadV?` som arver fra `StøttetVersjonertBarnetrygdSøknad`/
`StøttetVersjonertKontantstøtteSøknad`. Deretter må den nye versjonerte søknaden legges inn i den egendefinerte
serialiseringen og deserialiseringen, før man lager PR og releaser ny versjon av `familie-kontrakter`.

TLDR:

* Opprett ny mappe for versjon av søknadskontrakten.
* Opprett fil `BarnetrygdSøknad`/`KontantstøtteSøknad` under den nye mappa og definer ny `data class`
  `BarnetrygdSøknad`/`KontantstøtteSøknad` for søknadskontrakten som arver fra `BaSøknadBase`/`KsSøknadBase`.
* Opprett eventuelt nye `data class` for sub-typer som er endret i den nye versjonen av søknadskontrakten i samme fil.
* Opprett ny `data class` `VersjonertBarnetrygdSøknadV?`/`VersjonertKontantstøtteSøknadV?` som arver fra
  `StøttetVersjonertBarnetrygdSøknad`/`StøttetVersjonertKontantstøtteSøknad`.
* Legg inn den nye versjonerte søknaden i den egendefinerte serialiseringen og deserialiseringen.
* Opprett PR og release ny versjon av `familie-kontrakter`.

## Nødvendige backend-endringer etter opprettelse av ny versjon av søknadskontrakt

Ved release av ny versjon av søknadskontrakt, må man sørge for at alle tjenester som på en eller annen måte er relatert
til søknaden oppdateres med ny versjon av `familie-kontrakter`. I noen tjenester/apper er det nok å bumpe versjonen,
mens i andre kreves ytterligere kodeendringer.

Følgende apper må oppdateres:

* familie-baks-soknad-api
* familie-baks-mottak
* familie-baks-dokgen
* familie-integrasjoner
* familie-ba-sak / familie-ks-sak

Kortversjon av det som må gjøres i hver app:

* Oppdater `familie-kontrakter` til siste versjon for å få tilgang til ny versjon av søknadskontrakten.
* Oppdater eksisterende tester som tester på gammel versjon av søknadskontrakten, slik at de nå tester på den nye.
* Hvis app mottar søknad via endepunkt, sørg for at det opprettes nytt endepunkt for den nye versjonen ved siden av den
  nåværende/gamle (slik at vi støtter gammel versjon frem til søknadsdialog også er oppdatert).

### familie-baks-soknad-api

Det er her vi mottar innkommende søknader, så her må vi først og fremst legge til et nytt endepunkt for den nye
versjonen av søknaden ved siden av det eksisterende endepunktet som støtter gammel/nåværende versjon. Der skal søknaden
mottas og sendes videre til `familie-baks-mottak`. For tilgang til ny versjon av søknadskontrakt må `familie-kontrakter`
oppdateres til siste versjon. Sørg for at vi ikke støtter mer enn 2 versjoner av gangen.

### familie-baks-mottak

På lik linje som for `familie-baks-soknad-api` må det også her legges til et nytt endepunkt for den nye versjonen av
søknaden ved siden av det eksisterende. Eventuelle endepunkter for eldre versjoner bør fjernes, slik at det kun ligger
støtte for nåværende og ny versjon.

Fra og med søknaden er mottatt i controlleren i `familie-baks-mottak` mappes den om til
`StøttetVersjonertBarnetrygdSøknad`/`StøttetVersjonertKontantstøtteSøknad`, og det er denne klassen som brukes videre i
flyten. I det nye endepunktet må man derfor sørge for at man oppretter `VersjonertBarnetrygdSøknadV?`/
`VersjonertKontantstøtteSøknadV?`, som arver av `StøttetVersjonertBarnetrygdSøknad`/
`StøttetVersjonertKontantstøtteSøknad`, før man sender det inn til øvrig logikk. For tilgang til ny versjon må
`familie-kontrakter` oppdateres til siste versjon. Med ny versjon av `familie-kontrakter` vil man få en rekke feil ved
bygg. Dette skyldes i hovedsak alle `when`-statements som ikke støtter siste versjon av kontrakten. Fiks alle disse
feilene.

Oppdater alle tester relatert til søknadsflyt som tester på gammel versjon av kontrakten, slik at de nå tester med ny
versjon.

### familie-baks-dokgen

Bibliotek for generering av HTML som vi senere sender inn i en HTML-til-PDF genererator. Ingen direkte kobling mot
`familie-kontrakter`, men dersom endringen man har lagt inn skal reflekteres i PDF eller den gjelder endring av noe som
tidligere lå i PDF, må man sørge for at relaterte handlebars-templates blir oppdatert i henhold til endringen.

Oppdatere tester.

### familie-integrasjoner

I `familie-integrasjoner` har vi satt opp endepunkter for å kunne hente `VersjonertBarnetrygdSøknad`/
`VersjonertKontantstøtteSøknad` ut fra en Journalpost i dokarkiv, gitt en `journalpostId`. For at det skal fungere for
en ny versjon av søknaden må `familie-kontrakter` oppdateres. Det finnes også kode for å kunne tilgangsstyre visning av
journalposter som inneholder søknad, som også vil kreve oppdatert `familie-kontrakter` for å kunne håndtere søknader på
ny versjon.

Også her lurt å oppdatere alle tester som tester på gammel versjon av kontrakten, slik at de nå tester med ny versjon.

### familie-ba-sak

For at `familie-ba-sak` skal kunne hente og lese innhold i ny versjon av søknad må `familie-kontrakter` oppdateres
til siste versjon, og ny `SøknadMapper` opprettes for den nye versjonen.

Skrive tester for ny `SøknadMapper`.

## Nødvendige frontend-endringer etter opprettelse av ny versjon av søknadskontrakt

Ingen endringer vil skje med innsendelse av søknad før frontend-app oppdateres til å støtte den nye versjonen av
søknadskontrakten. Det betyr at man kan og bør gjøre alle nødvendige oppdateringer i backend-apper først, så lenge man
ikke fjerner støtten til nåværende/gammel versjon. Når alt ligger tilrette i backend kan man starte jobben med
endringene i frontend. I frontend vil jobben kort og godt bestå i å skrive om output til å matche den nye
søknadskontrakten. Hvor det som har størst innvirkning er endringen av feltet `versjon` fra gammel til ny. Med en gang
man forsøker å sende inn en søknad med nytt versjonsnummer i feltet `versjon` vil alle backend-apper forvente at
søknaden har et bestemt format.

I tillegg til å endre på selve kontrakten i frontend og oppdatere versjonsnummer bør man også oppdatere `modellversjon`
i samme PR man har tenkt til å merge inn ved overgang til ny kontaktsversjon. `modellversjon` styrer hvorvidt vi skal
bevare mellomlagret søknad eller om vi skal tvinge søker til å starte søknaden på nytt. Man står i utgangspunktet litt
fritt til å bestemme om man skal bumpe den eller ikke, men dersom man ønsker at alle søknader fra et bestemt tidspunkt
skal være på den nye versjonen vil bumping av `modellversjon` sørge for dette.

## Teste generering av PDF etter endringer i søknadskontrakt

Alle endringer man gjør i søknadskontakten kan påvirke genereringen av søknads-PDF'en som genereres i
`familie-baks-mottak` ved hjelp av `familie-baks-dokgen` og `familie-dokument`. Derfor er det viktig at man tester at
PDF-genereringen fortsatt fungerer som den skal før man går "live". Det finnes tester i både `familie-baks-mottak` og i
`familie-baks-dokgen` som burde oppdateres i henhold til ny kontrakt. Man burde også manuelt teste PDF ved innsendelse i
dev.

Den største utfordringen her er å generere JSON-inputen til `familie-baks-dokgen` i henhold til ny kontrakt. Dette har
vi fått til tidligere ved å legge inn følgende kode rett før `return` i metoden `lagBarnetrygdPdf()`/
`lagKontantstøttePdf()`
i [PdfService](https://github.com/navikt/familie-baks-mottak/blob/main/src/main/kotlin/no/nav/familie/baks/mottak/s%C3%B8knad/PdfService.kt):

```kotlin
println(jsonMapper.writeValueAsString(barnetrygdSøknadMapForSpråk + ekstraFelterMap))
```

For å få dette til å fungere må man kjøre opp `familie-baks-soknad-api` og `familie-baks-mottak` lokalt, slik at man
treffer `familie-baks-mottak` ved innsendelse av lokal søknad. Eventuelt må man logge ut JSON som
`familie-baks-soknad-api` ville sendt til `familie-baks-mottak` også manuelt kalt på lokalt kjørende
`familie-baks-mottak`. Da må man isåfall midlertidig skru av krav om autentisering/token i `familie-baks-mottak`.

## Eksempel på PR'er relatert til oppdatering av søknadskontrakten for kontantstøtte til versjon 6

- familie-kontrakter (Steg 1. Alle andre endringer krever at disse endringene er rullet ut først.):
    1. https://github.com/navikt/familie-kontrakter/pull/1087
    2. https://github.com/navikt/familie-kontrakter/pull/1089
    3. https://github.com/navikt/familie-kontrakter/pull/1090
- familie-baks-soknad-api:
    - https://github.com/navikt/familie-baks-soknad-api/pull/480
- familie-baks-mottak:
    - https://github.com/navikt/familie-baks-mottak/pull/1482
- familie-dokgen:
    - https://github.com/navikt/familie-baks-dokgen/pull/248
- familie-integrasjoner:
    - https://github.com/navikt/familie-integrasjoner/pull/1191
- familie-ks-soknad (Denne må ikke merges før de andre endringene er rullet ut):
    - https://github.com/navikt/familie-ks-soknad/pull/1189

