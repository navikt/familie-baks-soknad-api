package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknadV10Validator
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknadV6Validator
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad as KontantstøtteSøknadV6

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(
    private val kontantstøtteSøknadService: KontantstøtteSøknadService,
    private val barnetrygdSøknadService: BarnetrygdSøknadService
) {
    private val logger = LoggerFactory.getLogger(SøknadController::class.java)
    protected val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @PostMapping("/soknad/v10")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV10
    ): ResponseEntity<Ressurs<Kvittering>> {
        val valideringsfeil = BarnetrygdSøknadV10Validator.valider(søknad)
        if (valideringsfeil.isNotEmpty()) {
            logger.info("Søknad av barnetrygd(v10) mottatt med ${valideringsfeil.size} valideringsfeil. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av barnetrygd-søknad feilet:\n $valideringsfeil")
        }
        return ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
    }

    @PostMapping("/soknad/v9")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV9
    ): ResponseEntity<Ressurs<Kvittering>> = ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))

    @PostMapping("/soknad/kontantstotte/v6")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV6
    ): ResponseEntity<Ressurs<Kvittering>> {
        val valideringsfeil = KontantstøtteSøknadV6Validator.valider(kontantstøtteSøknad)
        if (valideringsfeil.isNotEmpty()) {
            logger.info("Søknad av kontantstøtte(v6) mottatt med ${valideringsfeil.size} valideringsfeil. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av kontantstøtte-søknad feilet:\n $valideringsfeil")
        }

        return ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
    }
}
