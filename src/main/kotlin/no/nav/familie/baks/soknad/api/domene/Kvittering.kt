package no.nav.familie.baks.soknad.api.domene

import java.time.LocalDateTime

data class Kvittering(
    val tekst: String,
    val mottattDato: LocalDateTime
)
