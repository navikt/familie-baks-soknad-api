package no.nav.familie.baks.soknad.api.domene

import no.nav.familie.kontrakter.felles.Tema
import java.time.Period

enum class Ytelse(val aldersgrense: Aldersgrense, val tema: Tema) {
    BARNETRYGD(Aldersgrense(18, 0), Tema.BAR),
    KONTANTSTOTTE(Aldersgrense(2, 6), Tema.KON)
}

data class Aldersgrense(val years: Int, val months: Int) {
    fun toTotalMonths() = Period.of(years, months, 0).toTotalMonths()
}
