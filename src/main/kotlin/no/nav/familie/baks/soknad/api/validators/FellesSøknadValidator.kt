package no.nav.familie.baks.soknad.api.validators

import no.nav.familie.kontrakter.felles.søknad.Søknadsfelt

internal fun Søknadsfelt<out Any?>.validerLabel(
    length: Int = 200
) {
    this.label.values.forEach { label ->
        label.validerLabel(length)
    }
}

internal fun String.validerLabel(
    length: Int = 200
) {
    require(this.length < length) { "Tekstfelt(label) er for langt. $this" }
    require(!Regex("[<>\"]").containsMatchIn(this)) { "Tekstfelt(label) inneholder ugyldige tegn. $this " }
}

internal fun Søknadsfelt<out Any?>.validerVerdiITextfelt(
    length: Int = 200
) {
    this.verdi.values.forEach { verdi ->
        verdi.toString().validerVerdiITextfelt(length)
    }
}

internal fun String.validerVerdiITextfelt(
    length: Int = 200
) {
    require(this.length < length) { "Tekstfelt er for langt. $this " }
    require(!Regex("[<>'\"]").containsMatchIn(this)) { "Tekstfelt inneholder ugyldige tegn, $this " }
}
