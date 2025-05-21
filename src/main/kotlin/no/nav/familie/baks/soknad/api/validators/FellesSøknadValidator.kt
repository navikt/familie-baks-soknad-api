package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.felles.søknad.Søknadsfelt

internal fun validerLabel(
    textField: Søknadsfelt<out Any?>,
    length: Int = 200
) {
    textField.label.values.forEach { label ->
        validerLabel(label, length)
    }
}

internal fun validerLabel(
    label: String,
    length: Int = 200
) {
    require(label.length < length) { "Tekstfelt(label) er for langt. $label" }
    require(!Regex("[<>\"]").containsMatchIn(label)) { "Tekstfelt(label) inneholder ugyldige tegn. $label " }
}

internal fun validerVerdiITextfelt(
    textField: Søknadsfelt<out Any?>,
    length: Int = 200
) {
    textField.verdi.values.forEach { verdi ->
        validerTextfelt(verdi.toString(), length)
    }
}

internal fun validerTextfelt(
    verdi: String,
    length: Int = 200
) {
    require(verdi.length < length) { "Tekstfelt er for langt. $verdi " }
    require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn, $verdi " }
}
