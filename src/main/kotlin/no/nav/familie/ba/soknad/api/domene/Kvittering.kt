package no.nav.familie.ba.soknad.api.domene

import java.time.LocalDateTime

data class Kvittering(val tekst: String, val mottattDato: LocalDateTime)
