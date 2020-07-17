package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(private val pdlClient: PdlClient,
                                private val barnePdlClient: BarnePdlClient) {


    private fun assertUgradertAdresse(adresseBeskyttelse: List<Adressebeskyttelse>) {
        if (adresseBeskyttelse.any { it.gradering != ADRESSEBESKYTTELSEGRADERING.UGRADERT }) {
            throw GradertAdresseException()
        }
    }

    private fun hentBarn(personIdent: String): HentBarnResponse {
        val response = barnePdlClient.hentBarn(personIdent)
        return Result.runCatching {
            val adresseBeskyttelse = response.data.person!!.adressebeskyttelse
            assertUgradertAdresse(adresseBeskyttelse)

            HentBarnResponse(
                    navn = response.data.person.navn.first().fulltNavn(),
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
            return listOfNotNull(bostedsadresse.matrikkeladresse, bostedsadresse.vegadresse)
        }

        return if (søkerAdresse == null || barneAdresse == null) false
        else {
            val søkerAdresser = adresseListe(søkerAdresse)
            val barneAdresser = adresseListe(barneAdresse)
            søkerAdresser.any { barneAdresser.contains(it) }
        }
    }

    fun hentPersoninfo(personIdent: String): Person {
        val response = pdlClient.hentSøker(personIdent)
        return Result.runCatching {
            val adresseBeskyttelse = response.data.person!!.adressebeskyttelse
            assertUgradertAdresse(adresseBeskyttelse)

            val barn: Set<Barn> = response.data.person.familierelasjoner.filter { relasjon ->
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
