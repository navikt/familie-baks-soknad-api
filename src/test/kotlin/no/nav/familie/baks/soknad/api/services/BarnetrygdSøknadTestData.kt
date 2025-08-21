package no.nav.familie.baks.soknad.api.services

import no.nav.familie.kontrakter.ba.søknad.v1.SIVILSTANDTYPE
import no.nav.familie.kontrakter.ba.søknad.v1.SøknadAdresse
import no.nav.familie.kontrakter.ba.søknad.v4.Søknadstype
import no.nav.familie.kontrakter.ba.søknad.v5.RegistrertBostedType
import no.nav.familie.kontrakter.ba.søknad.v7.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ba.søknad.v7.Søknaddokumentasjon
import no.nav.familie.kontrakter.ba.søknad.v7.Søknadsvedlegg
import no.nav.familie.kontrakter.ba.søknad.v8.Barn
import no.nav.familie.kontrakter.ba.søknad.v10.Søker
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad
import no.nav.familie.kontrakter.felles.søknad.Søknadsfelt

object BarnetrygdSøknadTestData {
    fun barnetrygdSøknad(
        søker: Søker = søker(),
        barn: List<Barn> = barn()
    ) = BarnetrygdSøknad(
        antallEøsSteg = 3,
        kontraktVersjon = 10,
        søknadstype = Søknadstype.ORDINÆR,
        søker = søker,
        barn = barn,
        spørsmål = mapOf(),
        dokumentasjon =
            listOf(
                Søknaddokumentasjon(
                    dokumentasjonsbehov = Dokumentasjonsbehov.ANNEN_DOKUMENTASJON,
                    harSendtInn = false,
                    opplastedeVedlegg =
                        listOf(
                            Søknadsvedlegg(
                                dokumentId = "en-slags-uuid",
                                navn = "IMG 1337.png",
                                tittel = Dokumentasjonsbehov.ANNEN_DOKUMENTASJON
                            )
                        ),
                    dokumentasjonSpråkTittel = mapOf("nb" to "Bekreftelse fra barnevernet")
                )
            ),
        originalSpråk = "nb",
        teksterUtenomSpørsmål = mapOf(),
        finnesPersonMedAdressebeskyttelse = false
    )

    fun søker(): Søker =
        Søker(
            harEøsSteg = true,
            navn = søknadsfelt("navn", "Navn Navnessen"),
            ident = søknadsfelt("fødselsnummer", "1234578901"),
            statsborgerskap = søknadsfelt("statsborgerskap", listOf("NOR")),
            adressebeskyttelse = false,
            adresse =
                søknadsfelt(
                    "adresse",
                    SøknadAdresse(
                        adressenavn = null,
                        postnummer = null,
                        husbokstav = null,
                        bruksenhetsnummer = null,
                        husnummer = null,
                        poststed = null
                    )
                ),
            sivilstand = søknadsfelt("sivilstand", SIVILSTANDTYPE.GIFT),
            spørsmål = mapOf(),
            nåværendeSamboer = null,
            tidligereSamboere = listOf(),
            svalbardOppholdPerioder = listOf(),
            arbeidsperioderUtland = listOf()
        )

    fun barn(): List<Barn> =
        listOf(
            Barn(
                harEøsSteg = true,
                navn = søknadsfelt("Barnets fulle navn", "barn1"),
                ident = søknadsfelt("Fødselsnummer", "12345678999"),
                registrertBostedType = søknadsfelt("Skal ha samme adresse", RegistrertBostedType.REGISTRERT_ANNEN_ADRESSE),
                alder = søknadsfelt("alder", "4 år"),
                spørsmål = mapOf(),
                utenlandsperioder = listOf(),
                eøsBarnetrygdsperioder = listOf()
            ),
            Barn(
                harEøsSteg = false,
                navn = søknadsfelt("Barnets fulle navn", "barn2"),
                ident = søknadsfelt("Fødselsnummer", "12345678987"),
                registrertBostedType = søknadsfelt("Skal ha samme adresse", RegistrertBostedType.IKKE_FYLT_INN),
                alder = søknadsfelt("alder", "1 år"),
                spørsmål = mapOf(),
                utenlandsperioder = listOf(),
                eøsBarnetrygdsperioder = listOf()
            ),
            Barn(
                harEøsSteg = true,
                navn = søknadsfelt("Barnets fulle navn", "barn3"),
                ident = søknadsfelt("Fødselsnummer", "12345678988"),
                registrertBostedType = søknadsfelt("Skal ha samme adresse", RegistrertBostedType.REGISTRERT_SOKERS_ADRESSE),
                alder = søknadsfelt("alder", "2 år"),
                spørsmål = mapOf(),
                utenlandsperioder = listOf(),
                eøsBarnetrygdsperioder = listOf()
            )
        )

    fun <T> søknadsfelt(
        label: String,
        verdi: T
    ): Søknadsfelt<T> = Søknadsfelt(label = mapOf("nb" to label), verdi = mapOf("nb" to verdi))
}
