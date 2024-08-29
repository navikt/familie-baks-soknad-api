package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.ba.søknad.v8.Søknad as BarnetrygdSøknadV8
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v4.KontantstøtteSøknad as KontantstøtteSøknadV4
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5

@Service
class SøknadService(
    val mottakClient: MottakClient,
    val personopplysningerService: PersonopplysningerService
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

    @Deprecated("Vi bruker ny versjon av barnetrygdsøknad")
    fun mottaOgSendBarnetrygdsøknad(kontantstøtteSøknad: BarnetrygdSøknadV8): Ressurs<Kvittering> {
        val søknadMedIdentFraToken =
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
        return mottakClient.sendBarnetrygdSøknad(søknadMedIdentFraToken)
    }

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

    private fun finnesPersonMedAdressebeskyttelse(
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
