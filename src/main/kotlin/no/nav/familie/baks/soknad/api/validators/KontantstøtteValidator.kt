package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.ks.søknad.v1.TekstPåSpråkMap
import no.nav.familie.kontrakter.ks.søknad.v2.Omsorgsperson
import no.nav.familie.kontrakter.ks.søknad.v4.AndreForelder
import no.nav.familie.kontrakter.ks.søknad.v4.Barn
import no.nav.familie.kontrakter.ks.søknad.v4.Søker
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad
import kotlin.String

fun KontantstøtteSøknad.valider() {
    søker.validerSøker()
    barn.valider()

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

    validerTeksterTilPdf(teksterTilPdf)

    listOfNotNull(
        this.erNoenAvBarnaFosterbarn,
        this.søktAsylForBarn,
        this.oppholderBarnSegIInstitusjon,
        this.barnOppholdtSegTolvMndSammenhengendeINorge,
        this.erBarnAdoptert,
        this.mottarKontantstøtteForBarnFraAnnetEøsland,
        this.harEllerTildeltBarnehageplass,
        this.erAvdødPartnerForelder
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }
}

private fun Søker.validerSøker() {
    this.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    listOfNotNull(
        this.navn,
        this.statsborgerskap,
        this.sivilstand,
        this.adresse,
        this.borPåRegistrertAdresse,
        this.værtINorgeITolvMåneder,
        this.planleggerÅBoINorgeTolvMnd,
        this.yrkesaktivFemÅr,
        this.erAsylsøker,
        this.utenlandsoppholdUtenArbeid,
        this.arbeidIUtlandet,
        this.mottarUtenlandspensjon,
        this.arbeidINorge,
        this.pensjonNorge,
        this.andreUtbetalinger,
        this.adresseISøkeperiode
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }
    listOfNotNull(
        this.andreUtbetalingsperioder,
        this.pensjonsperioderNorge,
        this.idNummer,
        this.arbeidsperioderNorge,
        this.pensjonsperioderUtland,
        this.utenlandsperioder,
        this.arbeidsperioderUtland
    ).forEach { liste ->
        validerListeAvSøknadsfelt(liste)
    }
}

fun List<Barn>.valider() {
    this.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn,
            barn.registrertBostedType,
            barn.alder,
            barn.erFosterbarn,
            barn.oppholderSegIInstitusjon,
            barn.erAdoptert,
            barn.erAsylsøker,
            barn.boddMindreEnn12MndINorge,
            barn.kontantstøtteFraAnnetEøsland,
            barn.harBarnehageplass,
            barn.andreForelderErDød,
            barn.utbetaltForeldrepengerEllerEngangsstønad,
            barn.mottarEllerMottokEøsKontantstøtte,
            barn.pågåendeSøknadFraAnnetEøsLand,
            barn.pågåendeSøknadHvilketLand,
            barn.planleggerÅBoINorge12Mnd,
            barn.borFastMedSøker,
            barn.foreldreBorSammen,
            barn.søkerDeltKontantstøtte,
            barn.søkersSlektsforhold,
            barn.søkersSlektsforholdSpesifisering,
            barn.borMedAndreForelder,
            barn.borMedOmsorgsperson,
            barn.adresse
        ).forEach { textField ->
            validerLabelOgVerdi(textField)
        }

        listOfNotNull(
            barn.eøsKontantstøttePerioder,
            barn.barnehageplassPerioder,
            barn.utenlandsperioder,
            barn.idNummer
        ).forEach { textField ->
            validerListeAvSøknadsfelt(textField)
        }

        barn.andreForelder?.valider()
        barn.omsorgsperson?.valider()
        validerTeksterTilPdf(barn.teksterTilPdf)
    }
}

fun validerTeksterTilPdf(
    teksterTilPdf: Map<String, TekstPåSpråkMap>
) {
    teksterTilPdf.keys.forEach {
        it.validerVerdiITextfelt()
        teksterTilPdf[it]?.tekstPåSpråk?.values?.forEach { tekst -> tekst.validerVerdiITextfelt() }
    }
}

fun AndreForelder.valider() {
    listOfNotNull(
        this.kanIkkeGiOpplysninger,
        this.navn,
        this.fnr,
        this.fødselsdato,
        this.yrkesaktivFemÅr,
        this.arbeidUtlandet,
        this.utenlandsoppholdUtenArbeid,
        this.pensjonUtland,
        this.adresse,
        this.arbeidNorge,
        this.pensjonNorge,
        this.andreUtbetalinger,
        this.pågåendeSøknadFraAnnetEøsLand,
        this.pågåendeSøknadHvilketLand,
        this.kontantstøtteFraEøs
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }

    listOfNotNull(
        this.arbeidsperioderUtland,
        this.utenlandsperioder,
        this.pensjonsperioderUtland,
        this.idNummer,
        this.arbeidsperioderNorge,
        this.pensjonsperioderNorge,
        this.andreUtbetalingsperioder,
        this.eøsKontantstøttePerioder
    ).forEach { textField ->
        validerListeAvSøknadsfelt(textField)
    }
}

fun Omsorgsperson.valider() {
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
        this.kontantstøtteFraEøs
    ).forEach { textField ->
        validerLabelOgVerdi(textField)
    }

    listOfNotNull(
        this.arbeidsperioderUtland,
        this.arbeidsperioderNorge,
        this.pensjonsperioderUtland,
        this.pensjonsperioderNorge,
        this.andreUtbetalingsperioder,
        this.eøsKontantstøttePerioder
    ).forEach { textField ->
        validerListeAvSøknadsfelt(textField)
    }
}
