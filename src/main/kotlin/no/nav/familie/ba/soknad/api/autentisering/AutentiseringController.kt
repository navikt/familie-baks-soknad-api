package no.nav.familie.ba.soknad.api.autentisering

import io.micrometer.core.instrument.Metrics
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class AutentiseringController {

    val innlogget = Metrics.counter("innlogget")

    @GetMapping("/innlogget")
    fun verifiserAutentisering(): ResponseEntity<Ressurs<String>> {
        innlogget.increment()

        return ResponseEntity.ok(Ressurs.success("Autentisert kall"))
    }
}
