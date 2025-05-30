package no.nav.familie.baks.soknad.api.integrasjoner

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.clients.pdl.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.baks.soknad.api.clients.pdl.Adressebeskyttelse
import no.nav.familie.baks.soknad.api.clients.pdl.FAMILIERELASJONSROLLE
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFamilierelasjon
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFolkeregisteridentifikator
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFødselsDato
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.clients.pdl.PdlNavn
import no.nav.familie.baks.soknad.api.clients.pdl.PdlPerson
import no.nav.familie.baks.soknad.api.clients.pdl.PdlPersonData
import no.nav.familie.baks.soknad.api.clients.pdl.PdlSivilstand
import no.nav.familie.baks.soknad.api.clients.pdl.PdlStatsborgerskap
import no.nav.familie.baks.soknad.api.clients.pdl.SIVILSTANDSTYPE
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.Vegadresse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5

@Component
class ClientMocks {
    @Bean
    @Primary
    @Profile("mock-mottak")
    fun mockMottakClient(): MottakClient {
        val mockMottakClient = mockk<MottakClient>()
        every { mockMottakClient.ping() } just Runs
        every { mockMottakClient.sendBarnetrygdSøknad(any<BarnetrygdSøknadV9>()) } returns
            Ressurs.success(Kvittering("søknad mottatt OK", LocalDateTime.now()))
        every { mockMottakClient.sendKontantstøtteSøknad(any<KontantstøtteSøknadV5>()) } returns
            Ressurs.success(Kvittering("søknad mottatt OK", LocalDateTime.now()))
        return mockMottakClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockPdlClient(): PdlBrukerClient {
        val mockPdlClient = mockk<PdlBrukerClient>()

        every { mockPdlClient.ping() } just Runs
        every { mockPdlClient.hentPerson(any(), any()) } returns
            PdlHentPersonResponse(
                data =
                    PdlPerson(
                        person =
                            PdlPersonData(
                                navn =
                                    listOf(
                                        PdlNavn(
                                            fornavn = "Voksen",
                                            etternavn = "Voksnessen"
                                        )
                                    ),
                                forelderBarnRelasjon =
                                    listOf(
                                        PdlFamilierelasjon("23456789876", FAMILIERELASJONSROLLE.BARN),
                                        PdlFamilierelasjon("12345678987", FAMILIERELASJONSROLLE.BARN),
                                        PdlFamilierelasjon("12345678989", FAMILIERELASJONSROLLE.MOR)
                                    ),
                                bostedsadresse =
                                    listOf(
                                        Bostedsadresse(
                                            vegadresse =
                                                Vegadresse(
                                                    21,
                                                    "2",
                                                    "A",
                                                    "H0101",
                                                    "Solveien",
                                                    "",
                                                    "",
                                                    "2304"
                                                ),
                                            matrikkeladresse = null
                                        )
                                    ),
                                adressebeskyttelse = emptyList(),
                                statsborgerskap =
                                    listOf(
                                        PdlStatsborgerskap(
                                            land = "NOR"
                                        ),
                                        PdlStatsborgerskap(
                                            land = "AFG"
                                        )
                                    ),
                                sivilstand =
                                    listOf(
                                        PdlSivilstand(
                                            type = SIVILSTANDSTYPE.GIFT
                                        )
                                    ),
                                foedselsdato =
                                    listOf(
                                        PdlFødselsDato(
                                            "2020-02-25"
                                        )
                                    ),
                                doedsfall = emptyList(),
                                folkeregisteridentifikator =
                                    listOf(
                                        PdlFolkeregisteridentifikator(
                                            identifikasjonsnummer = "23058518298"
                                        )
                                    )
                            )
                    ),
                errors = null,
                extensions = null
            )
        return mockPdlClient
    }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun mockEkspAuthPdlClient(): PdlApp2AppClient {
        val mockPdlClient = mockk<PdlApp2AppClient>()

        every { mockPdlClient.hentPerson("12345678987", any()) } returns
            PdlHentPersonResponse(
                data =
                    PdlPerson(
                        person =
                            PdlPersonData(
                                navn = listOf(PdlNavn("Barn", etternavn = "Barnessen III")),
                                foedselsdato = listOf(PdlFødselsDato("2022-01-01")),
                                bostedsadresse =
                                    listOf(
                                        Bostedsadresse(
                                            vegadresse =
                                                Vegadresse(
                                                    21,
                                                    "2",
                                                    "A",
                                                    "H0101",
                                                    "Solveien",
                                                    "",
                                                    "",
                                                    "2304"
                                                ),
                                            ukjentBosted = null,
                                            matrikkeladresse = null
                                        )
                                    ),
                                adressebeskyttelse = emptyList(),
                                statsborgerskap = emptyList(),
                                sivilstand = emptyList(),
                                doedsfall = emptyList(),
                                folkeregisteridentifikator =
                                    listOf(
                                        PdlFolkeregisteridentifikator(identifikasjonsnummer = "12345678987")
                                    )
                            )
                    ),
                errors = null,
                extensions = null
            )
        every { mockPdlClient.hentPerson("23456789876", any()) } returns
            PdlHentPersonResponse(
                data =
                    PdlPerson(
                        person =
                            PdlPersonData(
                                navn = listOf(PdlNavn("Barn", etternavn = "Barnessen II")),
                                foedselsdato = listOf(PdlFødselsDato("2008-10-01")),
                                bostedsadresse =
                                    listOf(
                                        Bostedsadresse(
                                            vegadresse =
                                                Vegadresse(
                                                    21,
                                                    "2",
                                                    "A",
                                                    "H0101",
                                                    "Solveien",
                                                    "",
                                                    "",
                                                    "2304"
                                                ),
                                            ukjentBosted = null,
                                            matrikkeladresse = null
                                        )
                                    ),
                                adressebeskyttelse = listOf(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG)),
                                statsborgerskap = emptyList(),
                                sivilstand = emptyList(),
                                doedsfall = emptyList(),
                                folkeregisteridentifikator =
                                    listOf(
                                        PdlFolkeregisteridentifikator(identifikasjonsnummer = "23456789876")
                                    )
                            )
                    ),
                errors = null,
                extensions = null
            )
        // Catch-all så man kan teste manuell registrerting av barn i dialogen
        every { mockPdlClient.hentPerson(not(or("12345678987", "23456789876")), any()) } returns
            PdlHentPersonResponse(
                data =
                    PdlPerson(
                        person =
                            PdlPersonData(
                                navn = listOf(PdlNavn("Barn", etternavn = "Barnessen IV")),
                                foedselsdato = listOf(PdlFødselsDato("2008-10-01")),
                                bostedsadresse =
                                    listOf(
                                        Bostedsadresse(
                                            vegadresse =
                                                Vegadresse(
                                                    21,
                                                    "2",
                                                    "A",
                                                    "H0101",
                                                    "Solveien",
                                                    "",
                                                    "",
                                                    "2304"
                                                ),
                                            ukjentBosted = null,
                                            matrikkeladresse = null
                                        )
                                    ),
                                adressebeskyttelse = emptyList(),
                                statsborgerskap = emptyList(),
                                sivilstand = emptyList(),
                                doedsfall = emptyList(),
                                folkeregisteridentifikator =
                                    listOf(
                                        PdlFolkeregisteridentifikator(identifikasjonsnummer = "31051575728")
                                    )
                            )
                    ),
                errors = null,
                extensions = null
            )
        return mockPdlClient
    }
}
