package no.nav.familie.ba.soknad.api.personopplysning

import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    private fun hentBarn(personIdent: String): Barn {
        val response = pdlClient.hentBarn(personIdent)
        return Result.runCatching {
            Barn(ident = personIdent, navn = response.data.person!!.navn.first().fulltNavn(), fødselsdato = response.data.person!!.foedsel.first().foedselsdato!!, borMedSøker = true)
        }.fold(
                onSuccess = { it },
                onFailure = { throw it }
        )
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentSøker(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter { relasjon ->
                relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map { relasjon ->
                hentBarn(relasjon.relatertPersonsIdent)
            }.toSet()

            response.data.person.let {
                Person(navn = it.navn.first().fulltNavn(), barn = barn)
            }
        }.fold(
                onSuccess = { it },
                onFailure = { throw it }
        )
    }

}
