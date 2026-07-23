package no.nav.familie.baks.soknad.api

import no.nav.familie.baks.soknad.api.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.baks.soknad.api"])
class Launcher

fun main(args: Array<String>) {
    val app = SpringApplication(ApplicationConfig::class.java)
    app.run(*args)
}
