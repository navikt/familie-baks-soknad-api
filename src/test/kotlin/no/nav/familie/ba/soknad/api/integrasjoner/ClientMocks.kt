package no.nav.familie.ba.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ba.soknad.api.personopplysning.*
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
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
        every { mockPdlClient.hentSøker(any()) } returns PdlHentSøkerResponse(
                data = PdlSøker(person = PdlSøkerData(
                        navn = listOf(PdlNavn("Voksen", etternavn = "Voksnessen")),
                        familierelasjoner = listOf(
                                PdlFamilierelasjon("12345678987", FAMILIERELASJONSROLLE.BARN),
                                PdlFamilierelasjon("12345678989", FAMILIERELASJONSROLLE.MOR)
                        ),
                        bostedsadresse = listOf(Bostedsadresse(vegadresse = null, ukjentBosted = null, matrikkeladresse = null)),
                        adressebeskyttelse = emptyList()

                )),
                errors = null
        )
        return mockPdlClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockEkspAuthPdlClient(): EkspandertAutorisasjonPdlClient {
        val mockPdlClient = mockk<EkspandertAutorisasjonPdlClient>()

        every { mockPdlClient.hentBarn(any()) } returns PdlHentBarnResponse(
                data = PdlBarn(person = PdlBarnData(
                        navn = listOf(PdlNavn("Barn", etternavn = "Barnessen")),
                        foedsel = listOf(PdlFødselsDato("1990-01-01")),
                        bostedsadresse = listOf(Bostedsadresse(vegadresse = null, ukjentBosted = null, matrikkeladresse = null)),
                        adressebeskyttelse = emptyList()
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