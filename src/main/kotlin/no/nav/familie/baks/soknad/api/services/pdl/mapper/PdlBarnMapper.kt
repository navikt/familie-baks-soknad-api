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
        fun adresseListe(bostedsadresse: Bostedsadresse): List<Any?> =
            listOfNotNull(bostedsadresse.matrikkeladresse, bostedsadresse.vegadresse)

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
                val pdlPersonDataBarn = barnRespons.data.person!!
                val barnHarAdresseBeskyttelse = harPersonAdresseBeskyttelse(pdlPersonDataBarn.adressebeskyttelse)
                Barn(
                    ident =
                        when (barnHarAdresseBeskyttelse) {
                            true -> null
                            false ->
                                pdlPersonDataBarn.folkeregisteridentifikator.first().identifikasjonsnummer!!
                        },
                    navn =
                        when (barnHarAdresseBeskyttelse) {
                            true -> null
                            false ->
                                pdlPersonDataBarn.navn
                                    .firstOrNull()!!
                                    .fulltNavn()
                        },
                    fødselsdato =
                        pdlPersonDataBarn.foedselsdato
                            .firstOrNull()
                            ?.foedselsdato,
                    borMedSøker =
                        when (barnHarAdresseBeskyttelse) {
                            true -> false
                            false ->
                                borBarnMedSoeker(
                                    soekerAdresse = soekerAdresse,
                                    barneAdresser =
                                        pdlPersonDataBarn.bostedsadresse
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
