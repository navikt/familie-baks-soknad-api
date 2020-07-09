package no.nav.familie.ba.soknad.api.kontrakt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Kontrakt (
        val søknadstype: String
)