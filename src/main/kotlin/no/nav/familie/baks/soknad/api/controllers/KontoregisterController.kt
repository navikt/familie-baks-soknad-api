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
import org.springframework.web.client.HttpClientErrorException

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
        try {
            val fnr = EksternBrukerUtils.hentFnrFraToken()
            val kontoregisterResponseDto = kontoregisterClient.hentKontonummer(kontohaver = fnr)

            val body =
                if (kontoregisterResponseDto.kontonummer.isNotEmpty()) {
                    Ressurs.success(
                        data = kontoregisterResponseDto
                    )
                } else {
                    Ressurs.failure("Klarte ikke finne kontonummer")
                }

            return ResponseEntity.ok().body(body)
        } catch (e: HttpClientErrorException.NotFound) {
            return ResponseEntity.status(404).body(Ressurs.failure("Klarte ikke finne kontonummer"))
        }
    }
}
