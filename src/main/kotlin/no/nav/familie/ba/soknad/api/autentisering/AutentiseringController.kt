package no.nav.familie.ba.soknad.api.autentisering

import io.micrometer.core.instrument.Metrics
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.TEXT_PLAIN_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class AutentiseringController {

    val innlogget = Metrics.counter("innlogget")

    @GetMapping("/innlogget")
    fun verifiserAutentisering(): String {
        innlogget.increment()

        return "Autentisert kall"
    }
}
