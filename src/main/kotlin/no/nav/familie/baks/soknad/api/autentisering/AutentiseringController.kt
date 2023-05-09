package no.nav.familie.baks.soknad.api.autentisering

import io.micrometer.core.instrument.Metrics
import no.nav.familie.kontrakter.ba.søknad.v4.Søknadstype
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class AutentiseringController {

    val innloggetOrdinaerBarnetrygd = Metrics.counter("innlogget.ordinaer.barnetrygd")
    val innloggetUtvidetBarnetrygd = Metrics.counter("innlogget.utvidet.barnetrygd")
    val innloggetKontantstøtte = Metrics.counter("innlogget.kontantstotte")

    @GetMapping("/innlogget/barnetrygd")
    fun verifiserAutentiseringBarnetrygd(
        @RequestParam(required = false)
        søknadstype: Søknadstype?
    ): ResponseEntity<Ressurs<String>> {
        if (søknadstype == Søknadstype.UTVIDET) {
            innloggetUtvidetBarnetrygd.increment()
        } else {
            innloggetOrdinaerBarnetrygd.increment()
        }

        return ResponseEntity.ok(Ressurs.success("Autentisert kall"))
    }

    @GetMapping("/innlogget/kontantstotte")
    fun verifiserAutentiseringKontantstøtte(): ResponseEntity<Ressurs<String>> {
        innloggetKontantstøtte.increment()
        return ResponseEntity.ok(Ressurs.success("Autentisert kall"))
    }
}
