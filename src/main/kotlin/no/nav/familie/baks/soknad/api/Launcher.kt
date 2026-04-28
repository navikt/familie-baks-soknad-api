package no.nav.familie.baks.soknad.api

import no.nav.familie.baks.soknad.api.config.ApplicationConfig
import no.nav.familie.sikkerhet.context.FamilieFellesNavTokenSupportKonfigurasjon
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(scanBasePackages = ["no.nav.familie.baks.soknad.api"])
@EnableJwtTokenValidation
@Import(FamilieFellesNavTokenSupportKonfigurasjon::class)
class Launcher

fun main(args: Array<String>) {
    val app = SpringApplication(ApplicationConfig::class.java)
    app.run(*args)
}
