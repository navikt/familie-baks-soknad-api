package no.nav.familie.ba.soknad.api.services.pdl

import no.nav.familie.ba.soknad.api.clients.pdl.BarnePdlClient
import no.nav.familie.ba.soknad.api.clients.pdl.PdlClient
import no.nav.familie.ba.soknad.api.personopplysning.Barn
import no.nav.familie.ba.soknad.api.clients.pdl.HentBarnResponse
import no.nav.familie.ba.soknad.api.personopplysning.PdlHentBarnResponse
import no.nav.familie.ba.soknad.api.personopplysning.Person
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlBarnMapper
import no.nav.familie.ba.soknad.api.services.pdl.mapper.PdlMapper
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

class PersonopplysningerService(
        private val pdlClient: PdlClient,
        private val barnePdlClient: BarnePdlClient
) {

    fun hentPersoninfo(personIdent: String): Person? {
        val response = pdlClient.hentSøker(personIdent)
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
            val barnRespons = hentBarnResponse(barnePdlClient.hentBarn(identBarn))
            PdlBarnMapper.mapBarn(barnRespons, identBarn, sokerAdresse)
        }
                .toSet()
    }

    fun hentBarnResponse(barn: PdlHentBarnResponse): HentBarnResponse {
        return Result.runCatching {
            val adresseBeskyttelse = barn.data.person?.adressebeskyttelse
            PdlMapper.assertUgradertAdresse(adresseBeskyttelse)

            barn.data.person?.navn?.first()?.let {
                HentBarnResponse(
                        navn = it.fulltNavn(),
                        fødselsdato = barn.data.person.foedsel.first().foedselsdato!!,
                        adresse = barn.data.person.bostedsadresse.firstOrNull()
                )
            }
        }.fold(
                onSuccess = { it!! },
                onFailure = { throw it }
        )
    }
}
