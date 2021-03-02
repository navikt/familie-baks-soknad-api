package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import org.springframework.stereotype.Service

@Service
class PersonopplysningerService(
    private val pdlClient: PdlClient,
    private val barnePdlClient: BarnePdlClient
) {

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

            val statsborgerskap: List<Statborgerskap> = mapStatsborgerskap(response.data.person.statsborgerskap)
            val barn: Set<Barn> = mapBarn(response.data.person)

            response.data.person.let {
                Person(
                    navn = it.navn.first().fulltNavn(),
                    statsborgerskap = statsborgerskap,
                    barn = barn,
                    siviltstatus = Sivilstand(type = mapSivilstandType(it.sivilstatus.sivilstandType))
                )
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    private fun mapSivilstandType(sivilstandType: SIVILSTAND): SIVILSTANDTYPE {
        return when (sivilstandType) {
            SIVILSTAND.GIFT -> SIVILSTANDTYPE.GIFT
            SIVILSTAND.ENKE_ELLER_ENKEMANN -> SIVILSTANDTYPE.ENKE_ELLER_ENKEMANN
            SIVILSTAND.SKILT -> SIVILSTANDTYPE.SKILT
            SIVILSTAND.SEPARERT -> SIVILSTANDTYPE.SEPARERT
            SIVILSTAND.REGISTRERT_PARTNER -> SIVILSTANDTYPE.REGISTRERT_PARTNER
            SIVILSTAND.SEPARERT_PARTNER -> SIVILSTANDTYPE.SEPARERT_PARTNER
            SIVILSTAND.SKILT_PARTNER -> SIVILSTANDTYPE.SKILT_PARTNER
            SIVILSTAND.GJENLEVENDE_PARTNER -> SIVILSTANDTYPE.GJENLEVENDE_PARTNER
        }
    }


    private fun mapBarn(person: PdlSøkerData) =
        person.familierelasjoner.filter { relasjon ->
            relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
        }.map { relasjon ->
            val barneRespons = hentBarn(relasjon.relatertPersonsIdent)
            val borMedSøker = borMedSøker(
                søkerAdresse = person.bostedsadresse.firstOrNull(),
                barneAdresse = barneRespons.adresse
            )
            Barn(
                ident = relasjon.relatertPersonsIdent, navn = barneRespons.navn,
                fødselsdato = barneRespons.fødselsdato, borMedSøker = borMedSøker
            )
        }.toSet()

    private fun mapStatsborgerskap(statsborgerskap: List<PdlStatsborgerskap>): List<Statborgerskap> {
        return statsborgerskap.map {
            Statborgerskap(
                landkode = it.land
            )
        }
    }
}
