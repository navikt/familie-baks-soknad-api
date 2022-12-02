package no.nav.familie.baks.soknad.api.helsesjekk

import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/helse")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class HelsesjekkController(
    private val mottakClient: MottakClient,
    private val pdlClient: PdlApp2AppClient,
    private val kodeverkClient: KodeverkClient
) {

    @GetMapping("soknad-api")
    fun pingApi(): Ressurs<String> {
        return Ressurs.success("OK")
    }

    @GetMapping("pdl")
    fun pingPdl(): Ressurs<String> {
        return Result.runCatching { pdlClient.ping() }
            .fold(
                onSuccess = { Ressurs.success("Ping mot PDL-API OK") },
                onFailure = { throw it }
            )
    }

    @GetMapping("mottak")
    fun pingMottak(): Ressurs<String> {
        return Result.runCatching { mottakClient.ping() }
            .fold(
                onSuccess = { Ressurs.success("Ping mot mottak OK") },
                onFailure = { throw it }
            )
    }

    @GetMapping("kodeverk")
    fun pingKodeverk(): Ressurs<String> {
        return Result.runCatching { kodeverkClient.ping() }
            .fold(
                onSuccess = { Ressurs.success("Ping mot kodeverk OK") },
                onFailure = { throw it }
            )
    }
}
