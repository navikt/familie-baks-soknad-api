package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.ba.søknad.v8.AndreForelder
import no.nav.familie.kontrakter.ba.søknad.v8.AndreForelderUtvidet
import no.nav.familie.kontrakter.ba.søknad.v8.Barn
import no.nav.familie.kontrakter.ba.søknad.v8.Omsorgsperson
import no.nav.familie.kontrakter.ba.søknad.v8.Søker
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad

fun BarnetrygdSøknad.valider() {
    søker.valider()
    barn.valider()

    spørsmål.keys.forEach { spørmålId ->
        if (spørmålId == "lestOgForståttBekreftelse") {
            spørsmål[spørmålId]?.validerSøknadsfelt(400)
        } else {
            spørsmål[spørmålId]?.validerSøknadsfelt(200)
        }
    }

    teksterUtenomSpørsmål.values.forEach { tekst ->
        tekst.values.forEach { verdi ->
            verdi.validerLabel()
        }
    }

    dokumentasjon.forEach {
        it.dokumentasjonsbehov.name.validerVerdiITextfelt()
        it.dokumentasjonSpråkTittel.values.forEach { tittel ->
            tittel.validerVerdiITextfelt()
        }
        it.opplastedeVedlegg.forEach { vedlegg ->
            vedlegg.dokumentId.validerVerdiITextfelt()
            vedlegg.navn.validerVerdiITextfelt()
        }
    }
}

internal fun no.nav.familie.kontrakter.ba.søknad.v4.Søknadsfelt<Any>.validerSøknadsfelt(
    length: Int = 200
) {
    this.label.values.forEach { label ->
        label.validerLabel(length)
    }
    this.label.values.forEach { label ->
        label.validerVerdiITextfelt(length)
    }
}

private fun Søker.valider() {
    this.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    listOfNotNull(
        this.navn,
        this.statsborgerskap,
        this.adresse,
        this.sivilstand,
        this.nåværendeSamboer
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }
    listOfNotNull(
        this.tidligereSamboere,
        this.utenlandsperioder,
        this.andreUtbetalingsperioder,
        this.arbeidsperioderUtland,
        this.arbeidsperioderNorge,
        this.pensjonsperioderNorge,
        this.pensjonsperioderUtland,
        this.idNummer
    ).forEach { liste ->
        validerListeAvSøknadsfelt(liste)
    }
}

private fun List<Barn>.valider() {
    this.forEach { barn ->
        barn.valider()
    }
}

private fun Barn.valider() {
    this.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
    }
    listOfNotNull(
        this.navn,
        this.registrertBostedType,
        this.alder
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }
    listOfNotNull(
        this.utenlandsperioder,
        this.eøsBarnetrygdsperioder,
        this.idNummer
    ).forEach { textField ->
        validerListeAvSøknadsfelt(textField)
    }

    this.omsorgsperson?.valider()
    this.andreForelder?.valider()
}

private fun Omsorgsperson.valider() {
    listOfNotNull(
        this.navn,
        this.slektsforhold,
        this.slektsforholdSpesifisering,
        this.idNummer,
        this.adresse,
        this.arbeidUtland,
        this.arbeidNorge,
        this.pensjonUtland,
        this.pensjonNorge,
        this.andreUtbetalinger,
        this.pågåendeSøknadFraAnnetEøsLand,
        this.pågåendeSøknadHvilketLand,
        this.barnetrygdFraEøs
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }

    listOfNotNull(
        this.arbeidsperioderUtland,
        this.arbeidsperioderNorge,
        this.pensjonsperioderUtland,
        this.pensjonsperioderNorge,
        this.andreUtbetalingsperioder,
        this.eøsBarnetrygdsperioder
    ).forEach { liste ->
        validerListeAvSøknadsfelt(liste)
    }
}

private fun AndreForelder.valider() {
    listOfNotNull(
        this.kanIkkeGiOpplysninger,
        this.navn,
        this.fnr,
        this.fødselsdato,
        this.arbeidUtlandet,
        this.pensjonUtland,
        this.skriftligAvtaleOmDeltBosted,
        this.pensjonNorge,
        this.arbeidNorge,
        this.andreUtbetalinger,
        this.adresse,
        this.pågåendeSøknadFraAnnetEøsLand,
        this.pågåendeSøknadHvilketLand,
        this.barnetrygdFraEøs
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }

    listOfNotNull(
        this.idNummer,
        this.arbeidsperioderUtland,
        this.pensjonsperioderUtland,
        this.arbeidsperioderNorge,
        this.pensjonsperioderNorge,
        this.andreUtbetalingsperioder,
        this.eøsBarnetrygdsperioder
    ).forEach { liste ->
        validerListeAvSøknadsfelt(liste)
    }
    this.utvidet.valider()
}

private fun AndreForelderUtvidet.valider() {
    listOfNotNull(
        this.søkerHarBoddMedAndreForelder,
        this.søkerFlyttetFraAndreForelderDato
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }
}
