package no.nav.familie.ba.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ba.soknad.api.personopplysning.*
import org.assertj.core.groups.Tuple
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

        val voksenNavn = PdlNavn("Voksen", etternavn = "Voksnessen")
        val relasjonBarn = PdlFamilierelasjon("12345678987", FAMILIERELASJONSROLLE.BARN)
        val relasjonMor = PdlFamilierelasjon("12345678989", FAMILIERELASJONSROLLE.MOR)
        val barneNavn = PdlNavn("Barn", etternavn = "Barnessen")

        every { mockPdlClient.ping() } just Runs
        every { mockPdlClient.hentNavnOgRelasjoner(any()) } returns PdlHentPersonResponse(
                data = PdlPerson(person = PdlPersonData(
                        navn = listOf(voksenNavn),
                        familierelasjoner = listOf(relasjonBarn, relasjonMor)
                )),
                errors = null
        )
        every { mockPdlClient.hentNavn(any()) } returns PdlHentPersonResponse(
                data = PdlPerson(person = PdlPersonData(
                        navn = listOf(barneNavn),
                        familierelasjoner = emptyList()
                )),
                errors = null
        )
        return mockPdlClient
    }

}