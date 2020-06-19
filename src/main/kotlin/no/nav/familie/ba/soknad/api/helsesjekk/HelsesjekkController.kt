package no.nav.familie.ba.soknad.api.helsesjekk

import no.nav.familie.ba.soknad.api.integrasjoner.MottakClient
import no.nav.familie.ba.soknad.api.integrasjoner.PdlClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/helse")
class HelsesjekkController(private val mottakClient: MottakClient, private val pdlClient: PdlClient) {

    @GetMapping("soknad-api")
    fun pingApi(): Ressurs<String> {
        return Ressurs.success("OK")
    }

    @GetMapping("pdl")
    fun pingPdl(): Ressurs<String> {
        return Result.runCatching { pdlClient.ping() }
                .fold(
                        onSuccess = {
                            Ressurs.success("Ping mot PDL-API OK")
                        },
                        onFailure = {
                            Ressurs.failure(errorMessage = "Ping mot PDL-API feilet", error = it)
                        }
                )
    }

    @GetMapping("mottak")
    fun pingMottak(): Ressurs<String> {
        return Result.runCatching { mottakClient.ping() }
                .fold(
                        onSuccess = {
                            Ressurs.success("Ping mot mottak OK")
                        },
                        onFailure = {
                            Ressurs.failure(errorMessage = "Ping mot mottak feilet", error = it)
                        }
                )

    }
}