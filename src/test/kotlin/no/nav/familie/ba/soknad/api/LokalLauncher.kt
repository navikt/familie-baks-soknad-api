package no.nav.familie.ba.soknad.api

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(TokenGeneratorConfiguration::class)
@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.soknad.api"])
@EnableJwtTokenValidation
class LokalLauncher

fun main(args: Array<String>) {
    val springApp = SpringApplication(LokalLauncher::class.java)
    springApp.setAdditionalProfiles("lokal", "mock-mottak", "mock-pdl")
    springApp.run(*args)
}