package no.nav.familie.ba.soknad.api.personopplysning

import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    private fun hentNavn(personIdent: String): String {
        val response = pdlClient.hentNavn(personIdent)
        return Result.runCatching {
            response.data.person!!.navn.first().fulltNavn()
        }.fold(
                onSuccess = { it },
                onFailure = { throw it}
        )
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentNavnOgRelasjoner(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter {
                relasjon -> relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map {
                relasjon -> Barn(relasjon.relatertPersonsIdent, hentNavn(relasjon.relatertPersonsIdent))
            }.toSet()

            response.data.person.let {
                Person(navn = it.navn.first().fulltNavn(), barn = barn)
            }
        }.fold(
                onSuccess = { it },
                onFailure = { throw it}
        )
    }

}
