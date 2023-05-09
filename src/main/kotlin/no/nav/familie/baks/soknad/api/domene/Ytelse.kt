package no.nav.familie.baks.soknad.api.domene

import java.time.Period
import no.nav.familie.kontrakter.felles.Tema

enum class Ytelse(val aldersgrense: Aldersgrense, val tema: Tema, val behandlingsnummer: String) {
    BARNETRYGD(Aldersgrense(18, 0), Tema.BAR, "284"),
    KONTANTSTOTTE(Aldersgrense(2, 6), Tema.KON, "B278")
}

data class Aldersgrense(val years: Int, val months: Int) {

    fun toTotalMonths() = Period.of(years, months, 0).toTotalMonths()
}
