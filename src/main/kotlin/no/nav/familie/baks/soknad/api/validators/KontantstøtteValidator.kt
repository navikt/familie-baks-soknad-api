package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad

fun KontantstøtteSøknad.valider() {
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn,
            barn.adresse
        ).forEach { textField ->
            // valider alle verdier i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }

    // XSS prevention - sanitize text fields
    listOfNotNull(
        søker.navn,
        søker.statsborgerskap,
        søker.sivilstand,
        søker.adresse
    ).forEach { textField ->
        // valider alle verdier i tekstfelt
        validerVerdiITextfelt(textField)
        // valider alle labler i tekstfelt
        validerLabel(textField)
    }
    listOfNotNull(
        søker.andreUtbetalingsperioder,
        søker.pensjonsperioderNorge,
        søker.idNummer,
        søker.arbeidsperioderNorge,
        søker.pensjonsperioderUtland,
        søker.utenlandsperioder,
        søker.arbeidsperioderUtland
    ).forEach { liste ->
        liste.forEach { textField ->
            // valider alle verider i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }
}
