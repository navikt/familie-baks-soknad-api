package no.nav.familie.ba.soknad.api.services.pdl.mapper

import no.nav.familie.ba.soknad.api.personopplysning.Barn
import no.nav.familie.ba.soknad.api.clients.pdl.HentBarnResponse
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


    fun mapBarn(barnRespons: HentBarnResponse, fnr: String, soekerAdresse: Bostedsadresse?): Barn {
        return Barn(
                ident = fnr,
                adresse = PdlMapper.mapAdresser(barnRespons.adresse),
                navn = barnRespons.navn,
                fødselsdato = barnRespons.fødselsdato,
                borMedSøker = borBarnMedSoeker(
                        soekerAdresse = soekerAdresse,
                        barneAdresse = barnRespons.adresse
                )
        )

    }
}
