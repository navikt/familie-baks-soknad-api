package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import no.nav.familie.kontrakter.felles.personinfo.UkjentBosted
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    private fun hentBarn(personIdent: String): HentBarnResponse {
        val response = pdlClient.hentBarn(personIdent)
        return Result.runCatching {

            HentBarnResponse(navn = response.data.person!!.navn.first().fulltNavn(), fødselsdato = response.data.person!!.foedsel.first().foedselsdato!!, adresse = response.data.person!!.bostedsadresse.firstOrNull())
        }.fold(
                onSuccess = { it },
                onFailure = { throw it }
        )
    }

    private fun fraBostedsadresse(bostedsadresse: Bostedsadresse?): Any? {
        return if (bostedsadresse == null) {
            null
        } else if (bostedsadresse.vegadresse != null) {
            bostedsadresse.vegadresse!!
        } else if (bostedsadresse.matrikkeladresse != null) {
            bostedsadresse.matrikkeladresse!!
        } else if (bostedsadresse.ukjentBosted != null) {
            bostedsadresse.ukjentBosted!!
        } else {
            null
        }
    }

    fun borMedSøker(søkerAdresse: Bostedsadresse?, barneAdresse: Bostedsadresse?): Boolean {
        val sAdresse = fraBostedsadresse(søkerAdresse)
        val bAdresse = fraBostedsadresse(barneAdresse)

        return (sAdresse != null && sAdresse !is UkjentBosted && sAdresse == bAdresse)
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentSøker(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter { relasjon ->
                relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map { relasjon ->
                val barneRespons = hentBarn(relasjon.relatertPersonsIdent)
                val borMedSøker = borMedSøker(søkerAdresse = response.data.person!!.bostedsadresse.firstOrNull(), barneAdresse = barneRespons.adresse)
                Barn(ident = relasjon.relatertPersonsIdent, navn = barneRespons.navn, fødselsdato = barneRespons.fødselsdato, borMedSøker = borMedSøker)
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
