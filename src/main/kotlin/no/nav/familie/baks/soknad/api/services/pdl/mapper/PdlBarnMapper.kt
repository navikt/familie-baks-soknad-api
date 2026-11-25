package no.nav.familie.baks.soknad.api.services.pdl.mapper

import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.domene.Barn
import no.nav.familie.baks.soknad.api.services.pdl.mapper.PdlMapper.harPersonAdresseBeskyttelse
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.DeltBosted
import java.time.LocalDate

object PdlBarnMapper {
    fun borBarnMedSoeker(
        soekerAdresse: Bostedsadresse?,
        bostedsadresser: List<Bostedsadresse>,
        deltBosted: List<DeltBosted>,
        dagensDato: LocalDate
    ): Boolean {
        if (soekerAdresse == null) return false

        val alleBarnetsAdresser =
            bostedsadresser.flatMap { listOfNotNull(it.matrikkeladresse, it.vegadresse) } +
                deltBosted
                    .filter { it.gjelderPå(dagensDato) }
                    .flatMap { listOfNotNull(it.matrikkeladresse, it.vegadresse) }

        return listOfNotNull(soekerAdresse.matrikkeladresse, soekerAdresse.vegadresse).any { it in alleBarnetsAdresser }
    }

    fun mapBarn(
        barnRespons: PdlHentPersonResponse,
        soekerAdresse: Bostedsadresse?,
        dagensDato: LocalDate
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
                            true -> {
                                false
                            }

                            false -> {
                                borBarnMedSoeker(
                                    soekerAdresse = soekerAdresse,
                                    bostedsadresser =
                                        barnRespons.data.person.bostedsadresse
                                            .filterNotNull(),
                                    deltBosted = barnRespons.data.person.deltBosted,
                                    dagensDato = dagensDato
                                )
                            }
                        },
                    adressebeskyttelse = barnHarAdresseBeskyttelse
                )
            }.fold(
                onSuccess = { it },
                onFailure = { throw it }
            )

    private fun DeltBosted.gjelderPå(dato: LocalDate): Boolean {
        val startdato = startdatoForKontrakt
        val sluttdato = sluttdatoForKontrakt
        return (startdato == null || startdato <= dato) && (sluttdato == null || sluttdato >= dato)
    }
}
