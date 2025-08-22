package no.nav.familie.baks.soknad.api.services

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9

@Service
class BarnetrygdSøknadService(
    mottakClient: MottakClient,
    personopplysningerService: PersonopplysningerService
) : SøknadService(
        mottakClient,
        personopplysningerService
    ) {
    fun mottaOgSendBarnetrygdsøknad(barnetrygdSøknad: BarnetrygdSøknadV9): Ressurs<Kvittering> {
        val søknadMedIdentFraToken =
            barnetrygdSøknad.copy(
                søker =
                    barnetrygdSøknad.søker.copy(
                        ident =
                            barnetrygdSøknad.søker.ident.copy(
                                verdi =
                                    barnetrygdSøknad.søker.ident.verdi
                                        .mapValues { EksternBrukerUtils.hentFnrFraToken() }
                            )
                    ),
                finnesPersonMedAdressebeskyttelse =
                    finnesPersonMedAdressebeskyttelse(
                        barnetrygdSøknad.søker.adressebeskyttelse,
                        barnetrygdSøknad.barn.map {
                            it.ident.verdi.values
                                .first()
                        },
                        Ytelse.BARNETRYGD
                    )
            )
        return mottakClient.sendBarnetrygdSøknad(søknadMedIdentFraToken)
    }

    fun mottaOgSendBarnetrygdsøknad(barnetrygdSøknad: BarnetrygdSøknadV10): Ressurs<Kvittering> {
        val søknadMedIdentFraToken =
            barnetrygdSøknad.copy(
                søker =
                    barnetrygdSøknad.søker.copy(
                        ident =
                            barnetrygdSøknad.søker.ident.copy(
                                verdi =
                                    barnetrygdSøknad.søker.ident.verdi
                                        .mapValues { EksternBrukerUtils.hentFnrFraToken() }
                            )
                    ),
                finnesPersonMedAdressebeskyttelse =
                    finnesPersonMedAdressebeskyttelse(
                        barnetrygdSøknad.søker.adressebeskyttelse,
                        barnetrygdSøknad.barn.map {
                            it.ident.verdi.values
                                .first()
                        },
                        Ytelse.BARNETRYGD
                    )
            )
        return mottakClient.sendBarnetrygdSøknad(søknadMedIdentFraToken)
    }
}
