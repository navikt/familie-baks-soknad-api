package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
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
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v4.KontantstøtteSøknad as KontantstøtteSøknadV4
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(
    private val kontantstøtteSøknadService: KontantstøtteSøknadService,
    private val barnetrygdSøknadService: BarnetrygdSøknadService
) {
    @Deprecated("Bruk nytt endepunkt med oppdatert kontrakt /soknad/v9 ")
    @PostMapping("/soknad/v8")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV8
    ): ResponseEntity<Ressurs<Kvittering>> = ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))

    @PostMapping("/soknad/v9")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV9
    ): ResponseEntity<Ressurs<Kvittering>> = ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))

    @Deprecated("Bruk nytt endepunkt med oppdatert kontrakt /soknad/kontantstotte/v5 ")
    @PostMapping("/soknad/kontantstotte/v4")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV4
    ): ResponseEntity<Ressurs<Kvittering>> =
        ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))

    @PostMapping("/soknad/kontantstotte/v5")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV5
    ): ResponseEntity<Ressurs<Kvittering>> =
        ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
}
