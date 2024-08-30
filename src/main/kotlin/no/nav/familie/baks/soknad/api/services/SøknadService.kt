package no.nav.familie.baks.soknad.api.services

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService

abstract class SøknadService(
    val mottakClient: MottakClient,
    val personopplysningerService: PersonopplysningerService
) {
    fun finnesPersonMedAdressebeskyttelse(
        søkerHarAdressebeskyttelse: Boolean,
        barn: List<String>,
        ytelse: Ytelse
    ): Boolean =
        søkerHarAdressebeskyttelse ||
            barn.any {
                it.harAdressebeskyttelse(ytelse)
            }

    private fun String.harAdressebeskyttelse(ytelse: Ytelse): Boolean =
        personopplysningerService
            .hentPersoninfo(
                personIdent = this,
                ytelse = ytelse,
                somSystem = true
            ).adressebeskyttelse
}
