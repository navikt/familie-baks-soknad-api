package no.nav.familie.baks.soknad.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication(scanBasePackages = ["no.nav.familie.baks.soknad.api"])
class LokalLauncher

fun main(args: Array<String>) {
    SpringApplicationBuilder(LokalLauncher::class.java)
        .initializers(MockOAuth2ServerInitializer())
        .profiles(
            "lokal",
            "mock-pdl",
            "mock-kodeverk",
            "mock-mottak",
            "mock-kontoregister",
        ).run(*args)
}
