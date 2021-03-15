package no.nav.familie.ba.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.familie.ba.soknad.api.clients.pdl.PdlClient
import no.nav.familie.ba.soknad.api.domene.Kvittering
import no.nav.familie.ba.soknad.api.personopplysning.FAMILIERELASJONSROLLE
import no.nav.familie.ba.soknad.api.personopplysning.PdlFamilierelasjon
import no.nav.familie.ba.soknad.api.personopplysning.PdlFødselsDato
import no.nav.familie.ba.soknad.api.personopplysning.PdlHentPersonResponse
import no.nav.familie.ba.soknad.api.personopplysning.PdlNavn
import no.nav.familie.ba.soknad.api.personopplysning.PdlPerson
import no.nav.familie.ba.soknad.api.personopplysning.PdlPersonData
import no.nav.familie.ba.soknad.api.personopplysning.PdlSivilstand
import no.nav.familie.ba.soknad.api.personopplysning.PdlStatsborgerskap
import no.nav.familie.ba.soknad.api.personopplysning.SIVILSTANDSTYPE
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
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
        every { mockMottakClient.sendSøknad(any()) } returns Ressurs.success(Kvittering("søknad mottatt OK", LocalDateTime.now()))
        return mockMottakClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockPdlClient(): PdlClient {
        val mockPdlClient = mockk<PdlClient>()

        every { mockPdlClient.ping() } just Runs
        every { mockPdlClient.hentPerson(any()) } returns PdlHentPersonResponse(
            data = PdlPerson(
                person = PdlPersonData(
                    navn = listOf(
                        PdlNavn(
                            fornavn = "Voksen",
                            etternavn = "Voksnessen"
                        )
                    ),
                    familierelasjoner = listOf(
                        PdlFamilierelasjon("12345678987", FAMILIERELASJONSROLLE.BARN),
                        PdlFamilierelasjon("12345678989", FAMILIERELASJONSROLLE.MOR)
                    ),
                    bostedsadresse = listOf(
                        Bostedsadresse(
                            vegadresse = null,
                            ukjentBosted = null,
                            matrikkeladresse = null
                        )
                    ),
                    adressebeskyttelse = emptyList(),
                    statsborgerskap = listOf(
                        PdlStatsborgerskap(
                            land = "NOR"
                        )
                    ),
                    sivilstand = listOf(
                        PdlSivilstand(
                            type = SIVILSTANDSTYPE.GIFT
                        )
                    ),
                    foedsel = listOf(
                        PdlFødselsDato(
                            "2020-02-25"
                        )
                    )
                )
            ),
            errors = null
        )
        return mockPdlClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockEkspAuthPdlClient(): PdlClient {
        val mockPdlClient = mockk<PdlClient>()

        every { mockPdlClient.hentPerson(any()) } returns PdlHentPersonResponse(
            data = PdlPerson(
                person = PdlPersonData(
                    navn = listOf(PdlNavn("Barn", etternavn = "Barnessen III")),
                    foedsel = listOf(PdlFødselsDato("1990-01-01")),
                    bostedsadresse = listOf(
                        Bostedsadresse(
                            vegadresse = null,
                            ukjentBosted = null,
                            matrikkeladresse = null
                        )
                    ),
                    adressebeskyttelse = emptyList(),
                    statsborgerskap = emptyList(),
                    sivilstand = null
                )
            ),
            errors = null
        )
        return mockPdlClient
    }

    @Bean
    @Primary
    @Profile("mock-sts") fun stsRestClientMock(): StsRestClient {
        val mockStsClient = mockk<StsRestClient>()
        every { mockStsClient.systemOIDCToken } returns ("MOCKED-OIDC-TOKEN")
        return mockStsClient
    }
}
