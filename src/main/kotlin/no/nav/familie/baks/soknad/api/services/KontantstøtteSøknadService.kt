package no.nav.familie.baks.soknad.api.services

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.ks.søknad.v4.KontantstøtteSøknad as KontantstøtteSøknadV4
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5

@Service
class KontantstøtteSøknadService(
    mottakClient: MottakClient,
    personopplysningerService: PersonopplysningerService
) : SøknadService(
        mottakClient,
        personopplysningerService
    ) {
    @Deprecated("Vi bruker ny versjon av kontantstøttesøknad")
    fun mottaOgSendKontantstøttesøknad(kontantstøtteSøknad: KontantstøtteSøknadV4): Ressurs<Kvittering> {
        val kontantstøtteSøknadMedIdentFraToken =
            kontantstøtteSøknad.copy(
                søker =
                    kontantstøtteSøknad.søker.copy(
                        ident =
                            kontantstøtteSøknad.søker.ident.copy(
                                verdi =
                                    kontantstøtteSøknad.søker.ident.verdi
                                        .mapValues { EksternBrukerUtils.hentFnrFraToken() }
                            )
                    )
            )
        return mottakClient.sendKontantstøtteSøknad(kontantstøtteSøknadMedIdentFraToken)
    }

    fun mottaOgSendKontantstøttesøknad(kontantstøtteSøknad: KontantstøtteSøknadV5): Ressurs<Kvittering> {
        val kontantstøtteSøknadMedIdentFraToken =
            kontantstøtteSøknad.copy(
                søker =
                    kontantstøtteSøknad.søker.copy(
                        ident =
                            kontantstøtteSøknad.søker.ident.copy(
                                verdi =
                                    kontantstøtteSøknad.søker.ident.verdi
                                        .mapValues { EksternBrukerUtils.hentFnrFraToken() }
                            )
                    ),
                finnesPersonMedAdressebeskyttelse =
                    finnesPersonMedAdressebeskyttelse(
                        kontantstøtteSøknad.søker.adressebeskyttelse,
                        kontantstøtteSøknad.barn.map {
                            it.ident.verdi.values
                                .first()
                        },
                        Ytelse.KONTANTSTOTTE
                    )
            )
        return mottakClient.sendKontantstøtteSøknad(kontantstøtteSøknadMedIdentFraToken)
    }
}
