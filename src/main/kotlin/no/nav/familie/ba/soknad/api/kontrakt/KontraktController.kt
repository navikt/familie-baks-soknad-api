package no.nav.familie.ba.soknad.api.kontrakt

import no.nav.familie.ba.soknad.api.config.ApplicationConfig
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class KontraktController() {

    @PostMapping("/kontrakt")
    fun kontraktInfo(@RequestBody(required=true) kontrakt: Kontrakt): ResponseEntity<Ressurs<String>> {
        val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        log.info(kontrakt.toString())
        return ResponseEntity.ok(Ressurs.success("Kontrakt OK"))
    }
}

