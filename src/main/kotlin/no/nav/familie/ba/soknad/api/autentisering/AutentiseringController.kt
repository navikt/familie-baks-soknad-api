package no.nav.familie.ba.soknad.api.autentisering

import io.micrometer.core.instrument.Metrics
import no.nav.familie.kontrakter.ba.Søknadstype
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class AutentiseringController {

    val innlogget = Metrics.counter("innlogget")
    val innloggetUtvidet = Metrics.counter("innlogget.utvidet")

    @GetMapping("/innlogget")
    fun verifiserAutentisering(@RequestParam(required = false) søknadstype: Søknadstype?): ResponseEntity<Ressurs<String>> {
        if (søknadstype == Søknadstype.UTVIDET) innloggetUtvidet.increment() else innlogget.increment()

        return ResponseEntity.ok(Ressurs.success("Autentisert kall"))
    }
}
