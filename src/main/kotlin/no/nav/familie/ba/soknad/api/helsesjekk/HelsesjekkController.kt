package no.nav.familie.ba.soknad.api.helsesjekk

import no.nav.familie.ba.soknad.api.integrasjoner.MottakClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/helse")
class HelsesjekkController(private val mottakClient: MottakClient) {

    @GetMapping("soknad-api")
    fun pingApi(): Ressurs<String> {
        return Ressurs.success("OK")
    }

    @GetMapping("pdl")
    fun pingPdl(): Ressurs<String> {
        return Ressurs.success("OK")
    }

    @GetMapping("mottak")
    fun pingMottak(): Ressurs<String> {
        return Result.runCatching { mottakClient.ping() }
                .fold(
                        onSuccess = { Ressurs.success("Ping mot mottak OK") },
                        onFailure = { throw it }
                )
    }
}