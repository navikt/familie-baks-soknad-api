package no.nav.familie.baks.soknad.api.personopplysning

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.pdl.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.baks.soknad.api.clients.pdl.Adressebeskyttelse
import no.nav.familie.baks.soknad.api.clients.pdl.FAMILIERELASJONSROLLE
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlDoedsafall
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFamilierelasjon
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFolkeregisteridentifikator
import no.nav.familie.baks.soknad.api.clients.pdl.PdlFødselsDato
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.clients.pdl.PdlNavn
import no.nav.familie.baks.soknad.api.clients.pdl.PdlPerson
import no.nav.familie.baks.soknad.api.clients.pdl.PdlPersonData
import no.nav.familie.baks.soknad.api.clients.pdl.PdlStatsborgerskap
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.baks.soknad.api.services.pdl.mapper.PdlBarnMapper
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.Matrikkeladresse
import no.nav.familie.kontrakter.felles.personopplysning.UkjentBosted
import no.nav.familie.kontrakter.felles.personopplysning.Vegadresse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PersonopplysningerServiceTest {
    private lateinit var personopplysningerService: PersonopplysningerService
    private lateinit var pdlClient: PdlBrukerClient
    private lateinit var barnePdlClient: PdlApp2AppClient
    private lateinit var kodeverkClient: KodeverkClient
    private lateinit var kodeverkService: CachedKodeverkService
    private val mapper = jsonMapper
    private val clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("Europe/Oslo"))
    private val gyldigBostedAdresse =
        Bostedsadresse(
            matrikkeladresse = Matrikkeladresse(3, "E67", "tillegg", "1456", "1223")
        )

    @BeforeEach
    fun setUp() {
        pdlClient = mockk()
        barnePdlClient = mockk()
        kodeverkClient = mockk()
        kodeverkService = CachedKodeverkService(kodeverkClient)
        personopplysningerService = PersonopplysningerService(pdlClient, barnePdlClient, kodeverkService, clock)
    }

    @Test
    fun `hentPersonInfo skal kun returnere familierelasjoner av type BARN`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonBarn")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)

        assertEquals(1, person.barn.size)
        assertEquals("ENGASJERT FYR", person.barn.first().navn)
        assertEquals("23058518298", person.barn.first().ident)
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste hvis det er familierelasjoner, men ingen barn`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedRelasjonerIngenBarn")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `hentPersonInfo skal returnere ident`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedRelasjonerIngenBarn")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertEquals("23058518298", person.ident)
    }

    @Test
    fun `hentPersonInfo skal returnere liste med statsborgerskap hvis det er flere fra pdl`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereStatsborgerskap")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertEquals(person.statsborgerskap.size, 2)
        assertEquals(person.statsborgerskap[0].landkode, "NOR")
        assertEquals(person.statsborgerskap[1].landkode, "SWE")
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste hvis ingen familierelasjoner`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonUtenRelasjoner")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `aldersgrensen vurderes mot avtalt dato og ikke maskinklokken`() {
        val fødselsdato = LocalDate.now(clock).minusMonths(30).plusDays(1)

        assertTrue(personopplysningerService.erBarnetsAlderUnderAldersgrenseForYtelse(fødselsdato, Ytelse.KONTANTSTOTTE))
    }

    @ParameterizedTest
    @CsvSource(
        "BARNETRYGD, 2026-06-15, 2008-06-16, true",
        "BARNETRYGD, 2026-06-15, 2008-06-15, false",
        "BARNETRYGD, 2026-06-15, 2008-06-14, false",
        "KONTANTSTOTTE, 2026-06-15, 2023-12-16, true",
        "KONTANTSTOTTE, 2026-06-15, 2023-12-15, false",
        "KONTANTSTOTTE, 2026-06-15, 2023-12-14, false",
        "KONTANTSTOTTE, 2026-08-31, 2024-03-01, true",
        "KONTANTSTOTTE, 2026-08-31, 2024-02-29, false",
        "BARNETRYGD, 2026-02-28, 2008-02-29, true",
        "BARNETRYGD, 2026-03-01, 2008-02-29, false"
    )
    fun `aldersgrensen gjelder rundt bursdag, månedsslutt og skuddår`(
        ytelse: Ytelse,
        vurderingsdato: LocalDate,
        fødselsdato: LocalDate,
        forventet: Boolean
    ) {
        val fastKlokke = Clock.fixed(vurderingsdato.atStartOfDay(clock.zone).toInstant(), clock.zone)
        val service = PersonopplysningerService(pdlClient, barnePdlClient, kodeverkService, fastKlokke)

        assertEquals(forventet, service.erBarnetsAlderUnderAldersgrenseForYtelse(fødselsdato, ytelse))
    }

    @Test
    fun `systemoppslag bruker systemklienten uten å slå opp søkeren med brukertoken`() {
        every { barnePdlClient.hentPerson("system-subject", Ytelse.KONTANTSTOTTE) } returns pdlMockFor("pdlPersonUtenRelasjoner")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")

        personopplysningerService.hentPersoninfo("system-subject", Ytelse.KONTANTSTOTTE, somSystem = true)

        verify(exactly = 1) { barnePdlClient.hentPerson("system-subject", Ytelse.KONTANTSTOTTE) }
        verify(exactly = 0) { pdlClient.hentPerson(any(), any()) }
    }

    @Test
    fun `dødt barn filtreres bort uten å fjerne levende søsken`() {
        val levende = lagPdlHentPersonRespons("levende-barn", LocalDate.of(2020, 1, 1))
        val avdød = lagPdlHentPersonRespons("avdod-barn", LocalDate.of(2020, 1, 1))
        val avdødMedDødsdato =
            avdød.copy(
                data = avdød.data.copy(person = requireNotNull(avdød.data.person).copy(doedsfall = listOf(PdlDoedsafall("2026-01-01"))))
            )
        every { barnePdlClient.hentPerson("levende-barn", Ytelse.BARNETRYGD) } returns levende
        every { barnePdlClient.hentPerson("avdod-barn", Ytelse.BARNETRYGD) } returns avdødMedDødsdato

        val barn = personopplysningerService.hentBarnTilSoeker(listOf("levende-barn", "avdod-barn"), null, Ytelse.BARNETRYGD)

        assertEquals(setOf("levende-barn"), barn.map { it.ident }.toSet())
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste med barn, der barn er dod`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlBarnErDoed")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("23058518298", Ytelse.BARNETRYGD)
        assertTrue(person.barn.isEmpty())
        verify(exactly = 1) { barnePdlClient.hentPerson(any(), Ytelse.BARNETRYGD) }
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste med barn, der barn er over atten`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlBarnErOverAtten")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("23058518298", Ytelse.BARNETRYGD)
        assertTrue(person.barn.isEmpty())
        verify(exactly = 1) { barnePdlClient.hentPerson(any(), Ytelse.BARNETRYGD) }
    }

    @Test
    fun `borMedSøker skal returnere false når søkerAdressen er null`() {
        val søkerAdresse = null
        val barneAdresse = Bostedsadresse()
        val borMedSøker =
            PdlBarnMapper.borBarnMedSoeker(
                soekerAdresse = søkerAdresse,
                bostedsadresser = listOf(barneAdresse),
                deltBosted = emptyList(),
                dagensDato = LocalDate.now(clock)
            )

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere true når adressene til barn og søker er like`() {
        val borMedSøker =
            PdlBarnMapper.borBarnMedSoeker(
                soekerAdresse = gyldigBostedAdresse,
                bostedsadresser = listOf(gyldigBostedAdresse),
                deltBosted = emptyList(),
                dagensDato = LocalDate.now(clock)
            )

        assertTrue(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere false når søker og barn har ulik adresse, men lik type`() {
        val barneAdresse =
            gyldigBostedAdresse.copy(
                matrikkeladresse =
                    Matrikkeladresse(
                        1,
                        "E2",
                        "tillegg",
                        "1456",
                        "1223"
                    )
            )
        val borMedSøker =
            PdlBarnMapper.borBarnMedSoeker(
                soekerAdresse = gyldigBostedAdresse,
                bostedsadresser = listOf(barneAdresse),
                deltBosted = emptyList(),
                dagensDato = LocalDate.now(clock)
            )

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere false hvis søkerAdresse er ukjent`() {
        val ukjentAdresse = Bostedsadresse(ukjentBosted = UkjentBosted("oslo"))
        val borMedSøker =
            PdlBarnMapper.borBarnMedSoeker(
                soekerAdresse = ukjentAdresse,
                bostedsadresser = listOf(ukjentAdresse),
                deltBosted = emptyList(),
                dagensDato = LocalDate.now(clock)
            )

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere true hvis flere adresser finnes, og minst en matcher`() {
        val søkerAdresse =
            gyldigBostedAdresse.copy(
                vegadresse =
                    Vegadresse(
                        adressenavn = "adresse",
                        husbokstav = "A",
                        bruksenhetsnummer = "1",
                        husnummer = "1",
                        kommunenummer = "1",
                        matrikkelId = 1,
                        postnummer = "0101",
                        tilleggsnavn = "tillegg"
                    )
            )
        val borMedSøker =
            PdlBarnMapper.borBarnMedSoeker(
                soekerAdresse = søkerAdresse,
                bostedsadresser = listOf(gyldigBostedAdresse),
                deltBosted = emptyList(),
                dagensDato = LocalDate.now(clock)
            )

        assertTrue(borMedSøker)
    }

    @Test
    fun `hentPerson skal sette flagg om person har adressebeskyttelse`() {
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonUtenRelasjonerGradertAdresse")

        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertTrue(person.adressebeskyttelse)
    }

    @Test
    fun `hentPerson skal sette flagg om person har adressebeskyttelse, og returnere null om bruker har adresse`() {
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlSoekerHarAdresseOgAdressebeskyttelse")

        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertEquals(person.adresse, null)
        assertEquals(person.adressebeskyttelse, true)
    }

    private fun pdlMockFor(filNavn: String) =
        mapper.readValue(
            File(getFile("pdl/$filNavn.json")),
            PdlHentPersonResponse::class.java
        )

    private fun kodeverkMockFor(filNavn: String) =
        mapper.readValue(
            File(getFile("kodeverk/$filNavn.json")),
            KodeverkDto::class.java
        )

    private fun getFile(name: String): String = javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")

    @Test
    fun `hentPerson returnerer rett adresse fra pdl`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonBarn")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")

        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertEquals(person.adresse?.adressenavn, "1223")
        assertEquals(person.adresse?.husnummer, "E22")
        assertEquals(person.adresse?.husbokstav, "tillegg")
        assertEquals(person.adresse?.postnummer, "4971")
        assertEquals(person.adresse?.poststed, "SUNDEBRU")
    }

    @Test
    fun `hentPerson sine barn har adressebeskyttelse`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlBarnHarAdresseBeskyttelse")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")

        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `hentPerson sine returnerer rett matrikkeladresse`() {
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlMedMatrikkelAdresse")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("1", Ytelse.BARNETRYGD)
        assertEquals(person.adresse?.adressenavn, "Tilleggsnavn")
        assertEquals(person.adresse?.bruksenhetsnummer, "1456")
        assertEquals(person.adresse?.postnummer, "4971")
        assertEquals(person.adresse?.poststed, "SUNDEBRU")
    }

    @Test
    fun `hentPerson sine barn har adressebeskyttelse og barnets navn blir null`() {
        val barn = PdlBarnMapper.mapBarn(pdlMockFor("pdlBarnHarAdresseBeskyttelse"), gyldigBostedAdresse, LocalDate.now(clock))
        assertEquals(null, barn.navn)
        assertFalse(barn.borMedSøker)
        assertTrue(barn.adressebeskyttelse)
    }

    @ParameterizedTest
    @CsvSource("2024-12-31, false", "2025-01-01, true", "2026-12-31, true", "2027-01-01, false")
    fun `borMedSøker er true bare når avtalen om delt bosted hos søker gjelder i dag`(
        dagensDato: LocalDate,
        forventet: Boolean
    ) {
        // Arrange
        val fastKlokke = Clock.fixed(dagensDato.atStartOfDay(clock.zone).toInstant(), clock.zone)
        val service = PersonopplysningerService(pdlClient, barnePdlClient, kodeverkService, fastKlokke)
        every { pdlClient.hentPerson(any(), Ytelse.BARNETRYGD) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        every { barnePdlClient.hentPerson("12345678910", Ytelse.BARNETRYGD) } returns pdlMockFor("pdlBarnMedDeltBosted")
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")

        // Act
        val person = service.hentPersoninfo("1", Ytelse.BARNETRYGD)

        // Assert
        assertEquals(forventet, person.barn.single().borMedSøker)
    }

    @Test
    fun `hentPerson skal ikke returnere barn som er 2 år og 6 mnd`() {
        every { barnePdlClient.hentPerson("23042018298", Ytelse.KONTANTSTOTTE) } returns
            lagPdlHentPersonRespons(
                "23042018298",
                LocalDate.now(clock).minusYears(2).minusMonths(6)
            )
        every { pdlClient.hentPerson("23058518298", Ytelse.KONTANTSTOTTE) } returns
            lagPdlHentPersonRespons(
                "23058518298",
                LocalDate.of(1985, 5, 23),
                listOf(
                    PdlFamilierelasjon(
                        "23042018298",
                        FAMILIERELASJONSROLLE.BARN
                    )
                )
            )
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("23058518298", Ytelse.KONTANTSTOTTE)
        assertEquals(person.barn.size, 0)
    }

    @Test
    fun `hentPerson skal returnere barn som er 1 dag mindre enn 2 år og 6 mnd`() {
        every { barnePdlClient.hentPerson("23042018298", Ytelse.KONTANTSTOTTE) } returns
            lagPdlHentPersonRespons(
                "23042018298",
                LocalDate
                    .now(clock)
                    .minusYears(2)
                    .minusMonths(6)
                    .plusDays(1)
            )
        every { pdlClient.hentPerson("23058518298", Ytelse.KONTANTSTOTTE) } returns
            lagPdlHentPersonRespons(
                "23058518298",
                LocalDate.of(1985, 5, 23),
                listOf(
                    PdlFamilierelasjon(
                        "23042018298",
                        FAMILIERELASJONSROLLE.BARN
                    )
                )
            )
        every { kodeverkClient.hentPostnummer() } returns kodeverkMockFor("kodeverkPostnummerRespons")
        val person = personopplysningerService.hentPersoninfo("23058518298", Ytelse.KONTANTSTOTTE)
        assertEquals(1, person.barn.size)
    }

    fun lagPdlHentPersonRespons(
        fnr: String,
        fødselsdato: LocalDate,
        forelderBarnRelasjoner: List<PdlFamilierelasjon> = emptyList()
    ): PdlHentPersonResponse =
        PdlHentPersonResponse(
            data =
                PdlPerson(
                    person =
                        PdlPersonData(
                            navn = listOf(PdlNavn(fornavn = "ENGASJERT", etternavn = "FYR")),
                            adressebeskyttelse =
                                listOf(
                                    Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.UGRADERT)
                                ),
                            folkeregisteridentifikator = listOf(PdlFolkeregisteridentifikator(fnr)),
                            bostedsadresse =
                                listOf(
                                    Bostedsadresse(
                                        vegadresse =
                                            Vegadresse(
                                                3L,
                                                "E22",
                                                "A",
                                                "1456",
                                                "Testgate",
                                                kommunenummer = "12",
                                                tilleggsnavn = "Tilleggsnavn",
                                                postnummer = "4971"
                                            )
                                    )
                                ),
                            statsborgerskap = listOf(PdlStatsborgerskap("NOR")),
                            foedselsdato = listOf(PdlFødselsDato(fødselsdato.format(DateTimeFormatter.ISO_DATE))),
                            doedsfall = emptyList(),
                            sivilstand = emptyList(),
                            forelderBarnRelasjon = forelderBarnRelasjoner
                        )
                ),
            errors = null,
            extensions = null
        )
}
