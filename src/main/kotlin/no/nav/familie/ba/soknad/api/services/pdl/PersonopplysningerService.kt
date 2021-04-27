package no.nav.familie.ba.soknad.api.services.pdl

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import no.nav.familie.ba.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.ba.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.ba.soknad.api.clients.pdl.PdlDoedsafall
import no.nav.familie.ba.soknad.api.clients.pdl.PdlSystemClient
import no.nav.familie.ba.soknad.api.domene.Barn
import no.nav.familie.ba.soknad.api.domene.Person
import no.nav.familie.ba.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlBarnMapper
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlMapper
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(
    private val pdlClient: PdlBrukerClient,
    private val pdlSystemClient: PdlSystemClient,
    kodeverkClient: KodeverkClient
) {
    val kodeverkService = CachedKodeverkService(kodeverkClient)
    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentPerson(personIdent)
        val barnTilSoeker = hentBarnTilSoeker(
            fnrBarn = PdlMapper.mapFnrBarn(response.data.person!!.forelderBarnRelasjon),
            sokerAdresse = response.data.person.bostedsadresse.firstOrNull()
        )

        return response.data.person.let {
            PdlMapper.mapTilPersonInfo(it, barnTilSoeker, kodeverkService)
        }
    }

    fun hentBarnTilSoeker(fnrBarn: List<String>, sokerAdresse: Bostedsadresse?): Set<Barn> {
        return fnrBarn
            .map { ident -> pdlSystemClient.hentPerson(ident) }
            .filter {
                erBarnILive(it.data.person?.doedsfall) &&
                    erUnderAtten(parseIsoDato(it.data.person?.foedsel?.firstOrNull()?.foedselsdato))
            }
            .map { PdlBarnMapper.mapBarn(it, sokerAdresse, kodeverkService) }.toSet()
    }

    private fun erBarnILive(doedsfall: List<PdlDoedsafall>?): Boolean {
        return doedsfall?.firstOrNull()?.doedsdato == null
    }

    fun erUnderAtten(fødselsdato: LocalDate?): Boolean {
        if (fødselsdato == null) {
            return false
        }
        val alder = Period.between(fødselsdato, LocalDate.now())
        val alderIÅr = alder.years
        return alderIÅr <= 18
    }

    fun parseIsoDato(dato: String?): LocalDate? {
        return LocalDate.parse(dato, DateTimeFormatter.ISO_DATE)
    }
}
