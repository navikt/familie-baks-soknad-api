package no.nav.familie.ba.soknad.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.soknad.api"])
class DevLauncher

fun main(args: Array<String>) {
    val springApp = SpringApplication(DevLauncher::class.java)
    springApp.setAdditionalProfiles("dev", "mock-mottak", "mock-pdl")
    springApp.run(*args)
}