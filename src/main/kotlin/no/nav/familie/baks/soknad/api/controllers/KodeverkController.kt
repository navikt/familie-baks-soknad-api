package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
@RequestMapping(path = ["/api/kodeverk"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KodeverkController(
    private val cachedKodeverkService: CachedKodeverkService
) {
    @GetMapping("/alle-land")
    fun alleLand(): ResponseEntity<Ressurs<Map<String, String>>> =
        ResponseEntity.ok(
            Ressurs.success(
                cachedKodeverkService.hentAlleLand()
            )
        )

    @GetMapping("/eos-land")
    fun eøsLand(): ResponseEntity<Ressurs<Map<String, String>>> =
        ResponseEntity.ok(
            Ressurs.success(
                cachedKodeverkService.hentEØSLand()
            )
        )
}
