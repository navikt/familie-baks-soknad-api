package no.nav.familie.baks.soknad.api.domene

import java.time.Period

enum class Ytelse(val aldersgrense: Aldersgrense) {
    BARNETRYGD(Aldersgrense(18, 0)),
    KONTANTSTOTTE(Aldersgrense(2, 6))
}

data class Aldersgrense(val years: Int, val months: Int) {

    fun toTotalMonths() = Period.of(years, months, 0).toTotalMonths()
}
