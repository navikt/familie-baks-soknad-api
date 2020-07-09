package no.nav.familie.ba.soknad.api.kontrakt

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
@RequestMapping(path = ["/api"])
class KontraktController() {

    @PostMapping("/kontrakt")
    fun kontraktInfo(@RequestBody(required=true) kontrakt: Kontrakt): String {
        return "Kontrakt OK"
    }
}

