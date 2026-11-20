package no.nav.familie.baks.soknad.api.personopplysning

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.config.CacheConfig
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@SpringJUnitConfig(
    CacheConfig::class,
    CachedKodeverkService::class,
    PersonopplysningerService::class,
    PersonopplysningerCacheTest.TestConfig::class
)
@ActiveProfiles("person-cache-test")
class PersonopplysningerCacheTest {
    @Autowired
    private lateinit var service: PersonopplysningerService

    @Autowired
    private lateinit var pdlClient: PdlBrukerClient

    @Autowired
    private lateinit var kodeverkClient: KodeverkClient

    @Autowired
    private lateinit var kodeverkService: CachedKodeverkService

    @Autowired
    @Qualifier("dailyCache")
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun ryddCacheOgMocker() {
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
        clearMocks(pdlClient, kodeverkClient)
    }

    @Test
    fun `personoppslag gjenbruker postnummer fra cache`() {
        val person =
            requireNotNull(javaClass.getResourceAsStream("/pdl/pdlPersonUtenRelasjoner.json")).use {
                jsonMapper.readValue(it, PdlHentPersonResponse::class.java)
            }
        every { pdlClient.hentPerson("test-subject", Ytelse.BARNETRYGD) } returns person
        every { kodeverkClient.hentPostnummer() } returns KodeverkDto(emptyMap())

        val første = service.hentPersoninfo("test-subject", Ytelse.BARNETRYGD)
        val andre = service.hentPersoninfo("test-subject", Ytelse.BARNETRYGD)

        assertThat(første.adresse).isNotNull
        assertThat(andre).isEqualTo(første)
        verify(exactly = 1) { kodeverkClient.hentPostnummer() }
        verify(exactly = 2) { pdlClient.hentPerson("test-subject", Ytelse.BARNETRYGD) }
    }

    @Test
    fun `land og EØS-land har hver sin cache`() {
        fun kodeverk(
            kode: String,
            term: String
        ) = KodeverkDto(
            mapOf(kode to listOf(BetydningDto(LocalDate.of(2020, 1, 1), LocalDate.of(2099, 1, 1), mapOf("nb" to BeskrivelseDto(term, "")))))
        )
        every { kodeverkClient.hentAlleLand() } returns kodeverk("USA", "USA")
        every { kodeverkClient.hentEØSLand() } returns kodeverk("SWE", "Sverige")

        repeat(2) {
            assertThat(kodeverkService.hentAlleLand()).containsExactlyEntriesOf(mapOf("USA" to "USA"))
            assertThat(kodeverkService.hentEØSLand()).containsExactlyEntriesOf(mapOf("SWE" to "Sverige"))
        }

        verify(exactly = 1) { kodeverkClient.hentAlleLand() }
        verify(exactly = 1) { kodeverkClient.hentEØSLand() }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("person-cache-test")
    class TestConfig {
        @Bean
        fun clock(): Clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC)

        @Bean
        fun pdlClient(): PdlBrukerClient = mockk()

        @Bean
        fun pdlApp2AppClient(): PdlApp2AppClient = mockk()

        @Bean
        fun kodeverkClient(): KodeverkClient = mockk()
    }
}
