package no.nav.familie.baks.soknad.api.kontoregister

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.baks.soknad.api.clients.mottak.KontoregisterClient
import no.nav.familie.baks.soknad.api.clients.mottak.KontoregisterResponseDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class KontoregisterTestConfig {
    @Bean
    @Profile("mock-kontoregister")
    @Primary
    fun kontoregisterClientMock(): KontoregisterClient {
        val kontoregisterClient: KontoregisterClient = mockk()

        every { kontoregisterClient.hentKontonummer(any()) } returns
            KontoregisterResponseDto(
                "815.493.00"
            )
        return kontoregisterClient
    }
}
