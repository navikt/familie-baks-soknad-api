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
            val sivilstandType = mapSivilstandType(response.data.person.sivilstand)

            response.data.person.let {
                Person(
                    navn = it.navn.first().fulltNavn(),
                    statsborgerskap = statsborgerskap,
                    barn = barn,
                    siviltstatus = Sivilstand(sivilstandType)
                )
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    private fun mapSivilstandType(sivilstandType: List<PdlSivilstand>): SIVILSTANDTYPE? {
        return if (sivilstandType.isEmpty()) {
            null
        } else {
            return when (sivilstandType.first().type) {
                SIVILSTAND_TYPE.GIFT -> SIVILSTANDTYPE.GIFT
                SIVILSTAND_TYPE.ENKE_ELLER_ENKEMANN -> SIVILSTANDTYPE.ENKE_ELLER_ENKEMANN
                SIVILSTAND_TYPE.SKILT -> SIVILSTANDTYPE.SKILT
                SIVILSTAND_TYPE.SEPARERT -> SIVILSTANDTYPE.SEPARERT
                SIVILSTAND_TYPE.REGISTRERT_PARTNER -> SIVILSTANDTYPE.REGISTRERT_PARTNER
                SIVILSTAND_TYPE.SEPARERT_PARTNER -> SIVILSTANDTYPE.SEPARERT_PARTNER
                SIVILSTAND_TYPE.SKILT_PARTNER -> SIVILSTANDTYPE.SKILT_PARTNER
                SIVILSTAND_TYPE.GJENLEVENDE_PARTNER -> SIVILSTANDTYPE.GJENLEVENDE_PARTNER
            }

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
