package no.nav.familie.ba.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ba.soknad.api.personopplysning.*
import no.nav.familie.http.sts.StsRestClient
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
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
        every { mockPdlClient.hentNavnOgRelasjoner(any()) } returns PdlHentPersonResponse(
                data = PdlPerson(person = PdlPersonData(
                        navn = listOf(PdlNavn("Voksen", etternavn = "Voksnessen")),
                        familierelasjoner = listOf(
                                PdlFamilierelasjon("12345678987", FAMILIERELASJONSROLLE.BARN),
                                PdlFamilierelasjon("12345678989", FAMILIERELASJONSROLLE.MOR)
                        )
                )),
                errors = null
        )
        every { mockPdlClient.hentNavn(any()) } returns PdlHentPersonResponse(
                data = PdlPerson(person = PdlPersonData(
                        navn = listOf(PdlNavn("Barn", etternavn = "Barnessen")),
                        familierelasjoner = emptyList()
                )),
                errors = null
        )
        return mockPdlClient
    }

    @Bean
    @Primary
    @Profile("mock-sts") fun stsRestClientMock(): StsRestClient {
        val mockStsClient = mockk<StsRestClient>()
        every {mockStsClient.systemOIDCToken} returns ("MOCKED-OIDC-TOKEN")
        return mockStsClient
    }


}