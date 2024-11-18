package no.nav.familie.baks.soknad.api.services.pdl.mapper

import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.domene.Barn
import no.nav.familie.baks.soknad.api.services.pdl.mapper.PdlMapper.harPersonAdresseBeskyttelse
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

object PdlBarnMapper {
    fun borBarnMedSoeker(
        soekerAdresse: Bostedsadresse?,
        barneAdresser: List<Bostedsadresse>
    ): Boolean {
        fun adresseListe(bostedsadresse: Bostedsadresse): List<Any?> = listOfNotNull(bostedsadresse.matrikkeladresse, bostedsadresse.vegadresse)

        return if (soekerAdresse == null || barneAdresser.isEmpty()) {
            false
        } else {
            val alleBarnetsAdresser = barneAdresser.flatMap { adresseListe(it) }
            adresseListe(soekerAdresse).any { alleBarnetsAdresser.contains(it) }
        }
    }

    fun mapBarn(
        barnRespons: PdlHentPersonResponse,
        soekerAdresse: Bostedsadresse?
    ): Barn =
        Result
            .runCatching {
                val barnHarAdresseBeskyttelse = harPersonAdresseBeskyttelse(barnRespons.data.person?.adressebeskyttelse)
                Barn(
                    ident =
                        barnRespons.data.person
                            ?.folkeregisteridentifikator
                            ?.first()
                            ?.identifikasjonsnummer!!,
                    navn =
                        if (barnHarAdresseBeskyttelse) {
                            null
                        } else {
                            barnRespons.data.person.navn
                                .firstOrNull()!!
                                .fulltNavn()
                        },
                    fødselsdato =
                        barnRespons.data.person.foedselsdato
                            .firstOrNull()
                            ?.foedselsdato,
                    borMedSøker =
                        when (barnHarAdresseBeskyttelse) {
                            true -> false
                            false ->
                                borBarnMedSoeker(
                                    soekerAdresse = soekerAdresse,
                                    barneAdresser =
                                        barnRespons.data.person.bostedsadresse
                                            .filterNotNull()
                                )
                        },
                    adressebeskyttelse = barnHarAdresseBeskyttelse
                )
            }.fold(
                onSuccess = { it },
                onFailure = { throw it }
            )
}
