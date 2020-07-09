package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    private fun hentBarn(personIdent: String): HentBarnResponse {
        val response = pdlClient.hentBarn(personIdent)
        return Result.runCatching {

            HentBarnResponse(
                    navn = response.data.person!!.navn.first().fulltNavn(),
                    fødselsdato = response.data.person.foedsel.first().foedselsdato!!,
                    adresse = response.data.person.bostedsadresse.firstOrNull()
            )
        }.fold(
                onSuccess = { it },
                onFailure = { throw it }
        )
    }

    fun borMedSøker(søkerAdresse: Bostedsadresse?, barneAdresse: Bostedsadresse?): Boolean {
        fun adresseListe(bostedsadresse: Bostedsadresse): List<Any?> {
            return listOf(bostedsadresse.matrikkeladresse, bostedsadresse.vegadresse).filterNotNull()
        }

        return if (søkerAdresse == null || barneAdresse == null) false
        else adresseListe(barneAdresse).any{adresseListe(søkerAdresse).contains(it)}
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentSøker(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter { relasjon ->
                relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map { relasjon ->
                val barneRespons = hentBarn(relasjon.relatertPersonsIdent)
                val borMedSøker = borMedSøker(søkerAdresse = response.data.person.bostedsadresse.firstOrNull(), barneAdresse = barneRespons.adresse)
                Barn(ident = relasjon.relatertPersonsIdent, navn = barneRespons.navn,
                        fødselsdato = barneRespons.fødselsdato, borMedSøker = borMedSøker)
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
