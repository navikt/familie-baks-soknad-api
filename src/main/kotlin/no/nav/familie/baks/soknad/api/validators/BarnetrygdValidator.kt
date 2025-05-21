package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad

fun BarnetrygdSøknad.valider() {
    // valider ident (forhindre SQL/NoSQL injection)
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn
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
        søker.adresse,
        søker.nåværendeSamboer
    ).forEach { textField ->
        // valider alle labler i tekstfelt
        validerVerdiITextfelt(textField)
        // valider alle verdier i tekstfelt
        validerLabel(textField)
    }
    listOfNotNull(
        søker.andreUtbetalingsperioder,
        søker.pensjonsperioderNorge,
        søker.idNummer,
        søker.arbeidsperioderNorge,
        søker.pensjonsperioderUtland,
        søker.tidligereSamboere,
        søker.utenlandsperioder,
        søker.arbeidsperioderUtland
    ).forEach { liste ->
        liste.forEach { søknadsfelt ->
            val mapAvLabels = søknadsfelt.label
            søknadsfelt.label.keys.forEach { locale ->
                validerLabel(mapAvLabels.getValue(locale))
            }
            val mapAvVerdier = søknadsfelt.verdi
            mapAvVerdier.keys.forEach { locale ->
                validerTextfelt(mapAvLabels.getValue(locale))
            }
        }
    }
}
