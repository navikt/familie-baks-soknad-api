package no.nav.familie.ba.soknad.api.kodeverk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.io.IOException
import no.nav.familie.ba.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

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
            objectMapper.readValue(fileResponseBody)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av $filename", e)
        }
    }

    private fun getFile(filename: String): String {
        return javaClass.classLoader?.getResource("kodeverk/$filename")?.file
               ?: error("Testkonfigurasjon feil")
    }
}
