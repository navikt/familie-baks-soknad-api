package no.nav.familie.baks.soknad.api.services.pdl

import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlDoedsafall
import no.nav.familie.baks.soknad.api.domene.Barn
import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.baks.soknad.api.services.pdl.mapper.PdlBarnMapper
import no.nav.familie.baks.soknad.api.services.pdl.mapper.PdlMapper
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Service
class PersonopplysningerService(
    private val pdlClient: PdlBrukerClient,
    private val pdlApp2AppClient: PdlApp2AppClient,
    kodeverkClient: KodeverkClient
) {
    val kodeverkService = CachedKodeverkService(kodeverkClient)

    fun hentPersoninfo(
        personIdent: String,
        ytelse: Ytelse,
        somSystem: Boolean = false
    ): Person {
        val response =
            if (somSystem) {
                pdlApp2AppClient.hentPerson(personIdent, ytelse)
            } else {
                pdlClient.hentPerson(personIdent, ytelse)
            }

        val barnTilSoeker =
            hentBarnTilSoeker(
                fnrBarn = PdlMapper.mapFnrBarn(response.data.person!!.forelderBarnRelasjon),
                sokerAdresse = response.data.person.bostedsadresse.firstOrNull(),
                ytelse
            )

        return response.data.person.let {
            PdlMapper.mapTilPersonInfo(it, barnTilSoeker, kodeverkService)
        }
    }

    fun hentBarnTilSoeker(
        fnrBarn: List<String>,
        sokerAdresse: Bostedsadresse?,
        ytelse: Ytelse
    ): Set<Barn> {
        return fnrBarn
            .map { ident -> pdlApp2AppClient.hentPerson(ident, ytelse) }
            .filter {
                erBarnILive(it.data.person?.doedsfall) &&
                    erBarnetsAlderUnderAldersgrenseForYtelse(
                        parseIsoDato(it.data.person?.foedsel?.firstOrNull()?.foedselsdato),
                        ytelse
                    )
            }
            .map { PdlBarnMapper.mapBarn(it, sokerAdresse) }.toSet()
    }

    private fun erBarnILive(doedsfall: List<PdlDoedsafall>?): Boolean {
        return doedsfall?.firstOrNull()?.doedsdato == null
    }

    fun erBarnetsAlderUnderAldersgrenseForYtelse(
        fødselsdato: LocalDate?,
        ytelse: Ytelse
    ): Boolean {
        if (fødselsdato == null) {
            return false
        }
        val alder = Period.between(fødselsdato, LocalDate.now())
        return alder.toTotalMonths() < ytelse.aldersgrense.toTotalMonths()
    }

    fun parseIsoDato(dato: String?): LocalDate? {
        return LocalDate.parse(dato, DateTimeFormatter.ISO_DATE)
    }
}
