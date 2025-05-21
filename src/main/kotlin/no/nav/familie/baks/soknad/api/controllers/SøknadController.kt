package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.baks.soknad.api.validators.valider
import no.nav.familie.kontrakter.felles.Ressurs
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
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
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
    private val logger = LoggerFactory.getLogger(SøknadController::class.java)
    protected val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @PostMapping("/soknad/v9")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV9
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            søknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av barnetrygd-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av barnetrygd-søknad feilet", e)
        }
        return ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
    }

    @PostMapping("/soknad/kontantstotte/v5")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV5
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            kontantstøtteSøknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av kontantstøtte-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av kontantstøtte-søknad feilet", e)
        }

        return ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
    }
}
