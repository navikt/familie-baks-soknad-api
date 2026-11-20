package no.nav.familie.baks.soknad.api.controllers

import com.nimbusds.jwt.SignedJWT
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.baks.soknad.api.MockOAuth2ServerConfig
import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.config.ApplicationConfig
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDateTime

@SpringBootTest(
    classes = [ApplicationConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["FAMILIE_BAKS_MOTTAK_URL=https://mottak.test"]
)
@Import(SøknadHttpTest.TestConfig::class)
@ActiveProfiles("soknad-http-test")
class SøknadHttpTest : MockOAuth2ServerConfig() {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mottak: MottakStub

    @Autowired
    private lateinit var pdlClient: PdlApp2AppClient

    @Autowired
    private lateinit var kodeverkClient: KodeverkClient

    private val http = RestClient.create()

    @BeforeEach
    fun settOpp() {
        mottak.server.reset()
        clearMocks(pdlClient, kodeverkClient)
        val person =
            requireNotNull(javaClass.getResourceAsStream("/pdl/pdlPersonUtenRelasjoner.json")).use {
                jsonMapper.readValue(it, PdlHentPersonResponse::class.java)
            }
        every { pdlClient.hentPerson(any(), any()) } returns person
        every { kodeverkClient.hentPostnummer() } returns KodeverkDto(emptyMap())
    }

    @AfterEach
    fun verifiserMottak() {
        mottak.server.verify()
    }

    @Test
    fun `innsending bruker tokenidentitet i faktisk multipart og returnerer kvittering`() {
        val token = lagTestToken()
        val tokenIdent = SignedJWT.parse(token).jwtClaimsSet.subject
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        val kvittering = Kvittering("OK", LocalDateTime.of(2026, 6, 15, 12, 0))
        mottak.server
            .expect(requestTo("https://mottak.test/api/soknad/v10"))
            .andExpect(method(HttpMethod.POST))
            .andExpect { request ->
                val multipart = (request as MockClientHttpRequest).bodyAsString
                assertThat(multipart).contains("name=\"søknad\"")
                val json = multipart.substringAfter("\r\n\r\n").substringBeforeLast("\r\n--")
                val sendt = jsonMapper.readValue(json, BarnetrygdSøknad::class.java)
                assertThat(sendt.søker.ident.verdi.values).isNotEmpty.containsOnly(tokenIdent)
                assertThat(sendt.barn).isEqualTo(søknad.barn)
                assertThat(sendt.finnesPersonMedAdressebeskyttelse).isFalse
            }.andRespond(withSuccess(jsonMapper.writeValueAsString(Ressurs.success(kvittering)), MediaType.APPLICATION_JSON))

        val response =
            http
                .post()
                .uri("http://localhost:$port/api/soknad/v10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(søknad)
                .retrieve()
                .body<Ressurs<Kvittering>>()

        assertThat(response?.data).isEqualTo(kvittering)
    }

    @Test
    fun `ugyldig søknad avvises over HTTP uten kall til mottak`() {
        val søker = BarnetrygdSøknadTestData.søker().copy(navn = BarnetrygdSøknadTestData.søknadsfelt("navn", "Navn <>"))
        val response =
            http
                .post()
                .uri("http://localhost:$port/api/soknad/v10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${lagTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BarnetrygdSøknadTestData.barnetrygdSøknad(søker = søker))
                .retrieve()
                .onStatus({ it.is4xxClientError }) { _, _ -> }
                .toEntity(String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains("søker.navn.verdi")
    }

    @Test
    fun `mottaksfeil gir feilsvar gjennom faktisk exception-handler`() {
        mottak.server.expect(requestTo("https://mottak.test/api/soknad/v10")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        val response =
            http
                .post()
                .uri("http://localhost:$port/api/soknad/v10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${lagTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BarnetrygdSøknadTestData.barnetrygdSøknad())
                .retrieve()
                .onStatus({ it.is5xxServerError }) { _, _ -> }
                .toEntity(String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body).contains("\"status\":\"FEILET\"")
    }

    class MottakStub {
        private val builder = RestClient.builder()
        val server: MockRestServiceServer = MockRestServiceServer.bindTo(builder).build()
        val client = MottakClient("https://mottak.test", builder.build())
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Profile("soknad-http-test")
    class TestConfig {
        @Bean
        fun mottakStub(): MottakStub = MottakStub()

        @Bean
        @Primary
        fun testMottakClient(stub: MottakStub): MottakClient = stub.client

        @Bean
        @Primary
        fun testPdlApp2AppClient(): PdlApp2AppClient = mockk()

        @Bean
        @Primary
        fun testKodeverkClient(): KodeverkClient = mockk()
    }
}
