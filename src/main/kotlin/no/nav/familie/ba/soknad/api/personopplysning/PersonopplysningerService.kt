package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import no.nav.familie.kontrakter.felles.personinfo.UkjentBosted
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    private fun hentBarn(personIdent: String): HentBarnResponse {
        val response = pdlClient.hentBarn(personIdent)
        return Result.runCatching {

            HentBarnResponse(navn = response.data.person!!.navn.first().fulltNavn(), fødselsdato = response.data.person!!.foedsel.first().foedselsdato!!, adresse = Bostedsadresse(null, null, null))
        }.fold(
                onSuccess = { it },
                onFailure = { throw it }
        )
    }

    private fun fraBostedsadresse(bostedsadresse: Bostedsadresse?): Any? {
        if(bostedsadresse == null){
            return null
        }else if (bostedsadresse.vegadresse != null) {
            return bostedsadresse.vegadresse!!
        } else if (bostedsadresse.matrikkeladresse != null) {
            return bostedsadresse.matrikkeladresse!!
        } else if (bostedsadresse.ukjentBosted != null) {
            return bostedsadresse.ukjentBosted!!
        } else {
            return null
        }
    }

    fun borMedSøker(søkerAdresse : Bostedsadresse?, barneAdresse : Bostedsadresse?) : Boolean {
        val sAdresse = fraBostedsadresse(søkerAdresse)
        val bAdresse = fraBostedsadresse(barneAdresse)

        if (sAdresse != null && sAdresse !is UkjentBosted && sAdresse == bAdresse) {
            return true
        }
        return false
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentSøker(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter { relasjon ->
                relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map { relasjon ->
                val barneRepons = hentBarn(relasjon.relatertPersonsIdent)
                val borMedSøker = borMedSøker(søkerAdresse = response.data.person!!.bostedsadresse.firstOrNull(), barneAdresse = barneRepons.adresse)
                Barn(ident = relasjon.relatertPersonsIdent, navn = barneRepons.navn, fødselsdato = barneRepons.fødselsdato, borMedSøker = borMedSøker)
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
