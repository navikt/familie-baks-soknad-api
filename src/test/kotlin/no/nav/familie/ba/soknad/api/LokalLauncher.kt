package no.nav.familie.ba.soknad.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.soknad.api"])
class LokalLauncher

fun main(args: Array<String>) {
    val springApp = SpringApplication(LokalLauncher::class.java)
    springApp.setAdditionalProfiles("lokal", "mock-mottak", "mock-pdl")
    springApp.run(*args)
}