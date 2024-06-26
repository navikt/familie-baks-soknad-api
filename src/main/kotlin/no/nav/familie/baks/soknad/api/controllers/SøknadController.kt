package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ba.søknad.v8.Søknad as BarnetrygdSøknadV8
import no.nav.familie.kontrakter.ks.søknad.v4.KontantstøtteSøknad as KontantstøtteSøknadV4

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(
    private val mottakClient: MottakClient
) {
    @PostMapping("/soknad/v8")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV8
    ): ResponseEntity<Ressurs<Kvittering>> {
        val søknadMedIdentFraToken =
            søknad.copy(
                søker =
                    søknad.søker.copy(
                        ident =
                            søknad.søker.ident.copy(
                                verdi =
                                    søknad.søker.ident.verdi
                                        .mapValues { EksternBrukerUtils.hentFnrFraToken() }
                            )
                    )
            )

        return ResponseEntity.ok().body(mottakClient.sendBarnetrygdSøknad(søknadMedIdentFraToken))
    }

    @PostMapping("/soknad/kontantstotte/v4")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV4
    ): ResponseEntity<Ressurs<Kvittering>> {
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

        return ResponseEntity.ok().body(mottakClient.sendKontantstøtteSøknad(kontantstøtteSøknadMedIdentFraToken))
    }
}
