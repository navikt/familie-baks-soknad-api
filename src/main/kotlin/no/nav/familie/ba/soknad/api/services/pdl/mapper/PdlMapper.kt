package no.nav.familie.ba.soknad.api.services.pdl.mapper

import no.nav.familie.ba.soknad.api.clients.pdl.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.ba.soknad.api.clients.pdl.Adressebeskyttelse
import no.nav.familie.ba.soknad.api.clients.pdl.FAMILIERELASJONSROLLE
import no.nav.familie.ba.soknad.api.clients.pdl.PdlFamilierelasjon
import no.nav.familie.ba.soknad.api.clients.pdl.PdlPersonData
import no.nav.familie.ba.soknad.api.clients.pdl.PdlSivilstand
import no.nav.familie.ba.soknad.api.clients.pdl.PdlStatsborgerskap
import no.nav.familie.ba.soknad.api.clients.pdl.SIVILSTANDSTYPE
import no.nav.familie.ba.soknad.api.domene.Adresse
import no.nav.familie.ba.soknad.api.domene.Barn
import no.nav.familie.ba.soknad.api.domene.Person
import no.nav.familie.ba.soknad.api.domene.Sivilstand
import no.nav.familie.ba.soknad.api.domene.Statborgerskap
import no.nav.familie.ba.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.kontrakter.ba.søknad.SIVILSTANDTYPE
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

object PdlMapper {

    fun mapTilPersonInfo(
        person: PdlPersonData,
        barn: Set<Barn>,
        kodeverkService: CachedKodeverkService
    ): Person {

        val statsborgerskap: List<Statborgerskap> = mapStatsborgerskap(person.statsborgerskap)
        val sivilstandType = mapSivilstandType(person.sivilstand!!)

        val harBrukerAdressebeskyttelse = harPersonAdresseBeskyttelse(person.adressebeskyttelse)
        val adresse = if (!harBrukerAdressebeskyttelse)
            mapAdresser(person.bostedsadresse.firstOrNull(), kodeverkService)
        else null

        return Result.runCatching {
            Person(
                ident = person.folkeregisteridentifikator.firstOrNull()?.identifikasjonsnummer!!,
                navn = person.navn.first().fulltNavn(),
                statsborgerskap = statsborgerskap,
                sivilstand = Sivilstand(sivilstandType),
                adresse = adresse,
                barn = barn,
                adressebeskyttelse = harBrukerAdressebeskyttelse
            )
        }.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    fun mapFnrBarn(familierelasjoner: List<PdlFamilierelasjon>): List<String> {
        return familierelasjoner.filter { relasjon -> relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN }
            .map { it.relatertPersonsIdent }
    }

    private fun mapStatsborgerskap(statsborgerskap: List<PdlStatsborgerskap>): List<Statborgerskap> {
        return statsborgerskap.map {
            Statborgerskap(
                landkode = it.land
            )
        }.distinctBy {
            it.landkode
        }
    }

    fun mapAdresser(bostedsadresse: Bostedsadresse?, kodeverkService: CachedKodeverkService): Adresse? {
        if (bostedsadresse?.vegadresse != null) {
            return Adresse(
                adressenavn = bostedsadresse.vegadresse!!.adressenavn,
                postnummer = bostedsadresse.vegadresse!!.postnummer,
                husnummer = bostedsadresse.vegadresse!!.husnummer,
                husbokstav = bostedsadresse.vegadresse!!.husbokstav,
                bruksenhetnummer = bostedsadresse.vegadresse!!.bruksenhetsnummer,
                bostedskommune = null,
                poststed = kodeverkService.hentPostnummer().getOrDefault(bostedsadresse.vegadresse!!.postnummer, "")
            )
        }
        if (bostedsadresse?.matrikkeladresse != null) {
            return Adresse(
                adressenavn = bostedsadresse.matrikkeladresse!!.tilleggsnavn,
                postnummer = bostedsadresse.matrikkeladresse!!.postnummer,
                husnummer = null,
                husbokstav = null,
                bruksenhetnummer = bostedsadresse.matrikkeladresse!!.bruksenhetsnummer,
                bostedskommune = null,
                poststed = kodeverkService.hentPostnummer().getOrDefault(bostedsadresse.matrikkeladresse!!.postnummer, "")
            )
        }
        return null
    }

    private fun mapSivilstandType(sivilstandType: List<PdlSivilstand>): SIVILSTANDTYPE? {
        return if (sivilstandType.isEmpty()) {
            null
        } else {
            return when (sivilstandType.first().type) {
                SIVILSTANDSTYPE.GIFT -> SIVILSTANDTYPE.GIFT
                SIVILSTANDSTYPE.ENKE_ELLER_ENKEMANN -> SIVILSTANDTYPE.ENKE_ELLER_ENKEMANN
                SIVILSTANDSTYPE.SKILT -> SIVILSTANDTYPE.SKILT
                SIVILSTANDSTYPE.SEPARERT -> SIVILSTANDTYPE.SEPARERT
                SIVILSTANDSTYPE.REGISTRERT_PARTNER -> SIVILSTANDTYPE.REGISTRERT_PARTNER
                SIVILSTANDSTYPE.SEPARERT_PARTNER -> SIVILSTANDTYPE.SEPARERT_PARTNER
                SIVILSTANDSTYPE.SKILT_PARTNER -> SIVILSTANDTYPE.SKILT_PARTNER
                SIVILSTANDSTYPE.GJENLEVENDE_PARTNER -> SIVILSTANDTYPE.GJENLEVENDE_PARTNER
                SIVILSTANDSTYPE.UGIFT -> SIVILSTANDTYPE.UGIFT
                SIVILSTANDSTYPE.UOPPGITT -> SIVILSTANDTYPE.UOPPGITT
            }
        }
    }

    fun harPersonAdresseBeskyttelse(adresseBeskyttelse: List<Adressebeskyttelse>?): Boolean {

        if (!adresseBeskyttelse.isNullOrEmpty() &&
            adresseBeskyttelse.any {
                it.gradering != ADRESSEBESKYTTELSEGRADERING.UGRADERT
            }
        ) {
            return true
        }
        return false
    }
}
