package no.nav.familie.ba.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class ClientMocks {

    @Bean
    @Primary
    @Profile("mock-mottak")
    fun mockMottakClient(): MottakClient {
        val mockMottakClient = mockk<MottakClient>()
        every { mockMottakClient.ping() } just Runs
        return mockMottakClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockPdlClient(): PdlClient {
        val mockPdlClient = mockk<PdlClient>()
        every { mockPdlClient.ping() } just Runs
        return mockPdlClient
    }
}