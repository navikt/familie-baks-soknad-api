package no.nav.familie.baks.soknad.api.services

import no.nav.familie.kontrakter.ks.søknad.v1.RegistrertBostedType
import no.nav.familie.kontrakter.ks.søknad.v1.SIVILSTANDTYPE
import no.nav.familie.kontrakter.ks.søknad.v1.Søknadsfelt
import no.nav.familie.kontrakter.ks.søknad.v4.Barn
import no.nav.familie.kontrakter.ks.søknad.v4.Søker
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad

object KontantstøtteSøknadTestData {
    fun kontantstøtteSøknad(
        søker: Søker = søker(),
        barn: List<Barn> = barn()
    ): KontantstøtteSøknad =
        KontantstøtteSøknad(
            kontraktVersjon = 5,
            antallEøsSteg = 2,
            søker = søker,
            barn = barn,
            dokumentasjon = emptyList(),
            teksterTilPdf = emptyMap(),
            originalSpråk = "nb",
            erNoenAvBarnaFosterbarn = søknadsfelt("Noen fosterbarn", "NEI"),
            søktAsylForBarn = søknadsfelt("Søkt asyl for barn", "NEI"),
            oppholderBarnSegIInstitusjon = søknadsfelt("Barn i institusjon", "NEI"),
            barnOppholdtSegTolvMndSammenhengendeINorge = søknadsfelt("Sammenhengende i Norge i 12 mnd", "JA"),
            erBarnAdoptert = søknadsfelt("Er barn adoptert", "NEI"),
            mottarKontantstøtteForBarnFraAnnetEøsland = søknadsfelt("Kontantstøtte annet land", "NEI"),
            harEllerTildeltBarnehageplass = søknadsfelt("Har barnehageplass", "NEI"),
            erAvdødPartnerForelder = null,
            finnesPersonMedAdressebeskyttelse = false
        )

    fun søker(): Søker =
        Søker(
            harEøsSteg = false,
            ident = søknadsfelt("Fødselsnummer", "12345678910"),
            navn = søknadsfelt("Navn", "Ola Norman"),
            statsborgerskap = søknadsfelt("Statsborgerskap", listOf("Norge")),
            adresse = søknadsfelt("Adresse", null),
            adressebeskyttelse = false,
            sivilstand = søknadsfelt("Sivilstand", SIVILSTANDTYPE.SEPARERT),
            borPåRegistrertAdresse = søknadsfelt("Bor på registrert adresse", "JA"),
            værtINorgeITolvMåneder = søknadsfelt("Norge 12 mnd", "JA"),
            utenlandsoppholdUtenArbeid = søknadsfelt("Opphold i utlandet uten arbeid", "JA"),
            utenlandsperioder = emptyList(),
            planleggerÅBoINorgeTolvMnd = søknadsfelt("Planlegger å bo i Norge i 12 mnd", "JA"),
            yrkesaktivFemÅr = søknadsfelt("Yrkesaktiv 5 år", "JA"),
            erAsylsøker = søknadsfelt("Er asylsøker", "NEI"),
            arbeidIUtlandet = søknadsfelt("Arbeid i utlandet", "NEI"),
            mottarUtenlandspensjon = søknadsfelt("Mottar pensjon fra utlandet", "NEI"),
            arbeidsperioderUtland = emptyList(),
            pensjonsperioderUtland = emptyList(),
            arbeidINorge = søknadsfelt("Arbeid i Norge", "JA"),
            arbeidsperioderNorge = emptyList(),
            pensjonNorge = søknadsfelt("Pensjon i Norge", "NEI"),
            pensjonsperioderNorge = emptyList(),
            andreUtbetalingsperioder = emptyList(),
            idNummer = emptyList(),
            andreUtbetalinger = søknadsfelt("Andre utbetalinger", "NEI"),
            adresseISøkeperiode = søknadsfelt("Adresse i søknadsperiode", "Testgate 123")
        )

    fun barn(): List<Barn> =
        listOf(
            Barn(
                harEøsSteg = false,
                ident = søknadsfelt("Fødselsnummer", "12345678910"),
                navn = søknadsfelt("Navn", "Ola Norman"),
                registrertBostedType = søknadsfelt("Registert bosted", RegistrertBostedType.REGISTRERT_SOKERS_ADRESSE),
                alder = søknadsfelt("Alder", "2"),
                adresse = søknadsfelt("Adresse", "Ukjent"),
                utenlandsperioder = emptyList(),
                erAsylsøker = søknadsfelt("Er asylsøker", "NEI"),
                idNummer = emptyList(),
                andreForelder = null,
                andreForelderErDød = null,
                borMedAndreForelder = null,
                erFosterbarn = søknadsfelt("Er fosterbarn", "Nei"),
                erAdoptert = søknadsfelt("Er adpotert", "Nei"),
                omsorgsperson = null,
                oppholderSegIInstitusjon = søknadsfelt("Oppholder seg i institusjon", "Nei"),
                borMedOmsorgsperson = null,
                søkerDeltKontantstøtte = null,
                eøsKontantstøttePerioder = emptyList(),
                søkersSlektsforhold = null,
                foreldreBorSammen = null,
                borFastMedSøker = søknadsfelt("Bor fast med søker", "Ja"),
                kontantstøtteFraAnnetEøsland = søknadsfelt("Kontantstøtte fra annet EØS land", "Nei"),
                mottarEllerMottokEøsKontantstøtte = null,
                barnehageplassPerioder = emptyList(),
                boddMindreEnn12MndINorge = søknadsfelt("Bodd mindre enn 12 mnd i Norge", "Nei"),
                planleggerÅBoINorge12Mnd = null,
                søkersSlektsforholdSpesifisering = null,
                utbetaltForeldrepengerEllerEngangsstønad = null,
                teksterTilPdf = emptyMap(),
                harBarnehageplass = søknadsfelt("Barnehageplass", "Ja"),
                pågåendeSøknadHvilketLand = null,
                pågåendeSøknadFraAnnetEøsLand = null
            )
        )

    fun <T> søknadsfelt(
        label: String,
        verdi: T
    ): Søknadsfelt<T> = Søknadsfelt(label = mapOf("nb" to label), verdi = mapOf("nb" to verdi))
}
