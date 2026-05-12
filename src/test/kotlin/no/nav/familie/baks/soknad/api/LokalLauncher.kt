package no.nav.familie.baks.soknad.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication(scanBasePackages = ["no.nav.familie.baks.soknad.api"])
class LokalLauncher

/**
 * Denne settes til en fixed port for å kunne bruke samme port som familie-dokument
 */
private const val MOCK_OAUTH2_SERVER_PORT: String = "11588"

fun main(args: Array<String>) {
    SpringApplicationBuilder(LokalLauncher::class.java)
        .profiles(
            "lokal",
            "mock-pdl",
            "mock-kodeverk",
            "mock-mottak",
            "mock-kontoregister"
        ).properties(mapOf("mock-oauth2-server.port" to MOCK_OAUTH2_SERVER_PORT))
        .run(*args)
}
