package no.nav.familie.baks.soknad.api.kodeverk

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import tools.jackson.module.kotlin.readValue
import java.io.File
import java.io.IOException

@Configuration
class KodeverkTestConfig {
    @Bean
    @Profile("mock-kodeverk")
    @Primary
    fun kodeverkClientMock(): KodeverkClient {
        val kodeverkClient: KodeverkClient = mockk()
        every { kodeverkClient.hentPostnummer() } returns lastMockResponse(filename = "kodeverkPostnummerRespons.json")
        every { kodeverkClient.hentEØSLand() } returns lastMockResponse(filename = "kodeverkEOSLandResponse.json")
        every { kodeverkClient.hentAlleLand() } returns lastMockResponse(filename = "kodeverkAlleLandResponse.json")
        every { kodeverkClient.ping() } returns Unit
        return kodeverkClient
    }

    private fun lastMockResponse(filename: String): KodeverkDto {
        val fileResponseBody = File(getFile(filename = filename))
        return try {
            jsonMapper.readValue(fileResponseBody)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av $filename", e)
        }
    }

    private fun getFile(filename: String): String =
        javaClass.classLoader?.getResource("kodeverk/$filename")?.file
            ?: error("Testkonfigurasjon feil")
}
