package no.nav.familie.ba.soknad.api.services.pdl

import no.nav.familie.ba.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.ba.soknad.api.clients.pdl.PdlBrukerClient
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
            PdlMapper.mapTilPersonInfo(it, personIdent, barnTilSoeker, kodeverkService)
        }
    }

    fun hentBarnTilSoeker(fnrBarn: List<String>, sokerAdresse: Bostedsadresse?): Set<Barn> {
        return fnrBarn.map { identBarn ->
            val barnRespons = pdlSystemClient.hentPerson(identBarn)
            barnRespons.data.person.let {
                PdlBarnMapper.mapBarn(barnRespons, identBarn, sokerAdresse, kodeverkService)
            }
        }.toSet()
    }
}
