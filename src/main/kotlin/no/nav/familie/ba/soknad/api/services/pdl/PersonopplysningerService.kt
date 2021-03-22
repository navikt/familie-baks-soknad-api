package no.nav.familie.ba.soknad.api.services.pdl

import no.nav.familie.ba.soknad.api.clients.pdl.PdlClient
import no.nav.familie.ba.soknad.api.domene.Barn
import no.nav.familie.ba.soknad.api.domene.Person
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlBarnMapper
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlMapper
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(
    private val pdlClient: PdlClient
) {

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentPerson(personIdent)
        val barnTilSoeker = hentBarnTilSoeker(
            fnrBarn = PdlMapper.mapFnrBarn(response.data.person!!.familierelasjoner),
            sokerAdresse = response.data.person.bostedsadresse.firstOrNull()
        )

        return response.data.person.let {
            PdlMapper.mapTilPersonInfo(it, personIdent, barnTilSoeker)
        }
    }

    fun hentBarnTilSoeker(fnrBarn: List<String>, sokerAdresse: Bostedsadresse?): Set<Barn> {
        return fnrBarn.map { identBarn ->
            val barnRespons = pdlClient.hentPerson(identBarn)
            barnRespons.data.person.let {
                PdlBarnMapper.mapBarn(barnRespons, identBarn, sokerAdresse)
            }
        }.toSet()
    }
}
