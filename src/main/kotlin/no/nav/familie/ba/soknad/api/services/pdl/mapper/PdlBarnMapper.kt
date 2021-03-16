package no.nav.familie.ba.soknad.api.services.pdl.mapper

import no.nav.familie.ba.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.ba.soknad.api.personopplysning.Barn
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

object PdlBarnMapper {

    fun borBarnMedSoeker(soekerAdresse: Bostedsadresse?, barneAdresse: Bostedsadresse?): Boolean {
        fun adresseListe(bostedsadresse: Bostedsadresse): List<Any?> {
            return listOfNotNull(bostedsadresse.matrikkeladresse, bostedsadresse.vegadresse)
        }

        return if (soekerAdresse == null || barneAdresse == null) false
        else {
            val soekerAdresser = adresseListe(soekerAdresse)
            val barneAdresser = adresseListe(barneAdresse)
            soekerAdresser.any { barneAdresser.contains(it) }
        }
    }

    fun mapBarn(barnRespons: PdlHentPersonResponse, fnr: String, soekerAdresse: Bostedsadresse?): Barn {
        return Result.runCatching {
            val adresseBeskyttelse = barnRespons.data.person?.adressebeskyttelse
            PdlMapper.assertUgradertAdresse(adresseBeskyttelse)

            Barn(
                ident = fnr,
                adresse = PdlMapper.mapAdresser(barnRespons.data.person?.bostedsadresse?.firstOrNull()),
                navn = barnRespons.data.person?.navn?.firstOrNull()!!.fulltNavn(),
                fødselsdato = barnRespons.data.person.foedsel.firstOrNull()?.foedselsdato,
                borMedSøker = borBarnMedSoeker(
                    soekerAdresse = soekerAdresse,
                    barneAdresse = barnRespons.data.person.bostedsadresse.firstOrNull()
                )
            )
        }.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }
}
