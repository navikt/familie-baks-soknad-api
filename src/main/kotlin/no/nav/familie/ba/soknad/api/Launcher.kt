package no.nav.familie.ba.soknad.api

import no.nav.familie.ba.soknad.api.config.ApplicationConfig
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.soknad.api"])
@EnableJwtTokenValidation
class Launcher

fun main(args: Array<String>) {
    val app = SpringApplication(ApplicationConfig::class.java)
    app.run(*args)
}
