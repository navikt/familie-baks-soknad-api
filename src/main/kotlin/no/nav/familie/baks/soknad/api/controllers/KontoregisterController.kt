package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.clients.mottak.KontoregisterClient
import no.nav.familie.baks.soknad.api.clients.mottak.KontoregisterResponseDto
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
@RequestMapping(path = ["/api/kontoregister"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class KontoregisterController(
    private val kontoregisterClient: KontoregisterClient
) {
    @GetMapping("/hent-kontonr")
    fun hentKontoForInnloggetBruker(): ResponseEntity<Ressurs<KontoregisterResponseDto>> {
        val fnr = EksternBrukerUtils.hentFnrFraToken()
        return ResponseEntity.ok().body(kontoregisterClient.hentKontonummer(fnr))
    }
}
