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
            textField.validerVerdiITextfelt()
            // valider alle labler i tekstfelt
            textField.validerLabel()
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
        textField.validerVerdiITextfelt()
        // valider alle verdier i tekstfelt
        textField.validerLabel()
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
                mapAvLabels.getValue(locale).validerLabel()
            }
            val mapAvVerdier = søknadsfelt.verdi
            mapAvVerdier.keys.forEach { locale ->
                mapAvLabels.getValue(locale).validerVerdiITextfelt()
            }
        }
    }
}
