package no.nav.familie.baks.soknad.api.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class RestClientConfigTest {
    private val entraFactory = mockk<EntraIDRestClientFactory>()
    private val tokenX = mockk<TokenXClient>()
    private val consumerId = mockk<ConsumerIdClientInterceptor>()
    private val mdc = mockk<MdcValuesPropagatingClientInterceptor>()
    private val config = RestClientConfig(entraFactory, consumerId, mdc)

    @BeforeEach
    fun settOpp() {
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.getBearerTokenForLoggedInUser() } returns "subject-test-token"
        every { consumerId.intercept(any(), any(), any()) } answers {
            thirdArg<ClientHttpRequestExecution>().execute(firstArg(), secondArg())
        }
        every { mdc.intercept(any(), any(), any()) } answers {
            thirdArg<ClientHttpRequestExecution>().execute(firstArg(), secondArg())
        }
    }

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @ParameterizedTest
    @ValueSource(strings = ["pdl", "kontoregister", "mottak"])
    fun `brukerklienter utveksler token til riktig mottaker før HTTP-kallet`(klient: String) {
        val scope = "$klient-scope"
        every { tokenX.hentToken(scope, "subject-test-token") } returns "exchanged-test-token"
        val builder =
            when (klient) {
                "pdl" -> config.pdlTokenXRestClient(tokenX, scope)
                "kontoregister" -> config.kontoregisterTokenXRestClient(tokenX, scope)
                else -> config.mottakTokenXRestClient(tokenX, scope)
            }.mutate()
        val server = MockRestServiceServer.bindTo(builder).build()
        server
            .expect(requestTo("https://downstream.test/resource"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer exchanged-test-token"))
            .andRespond(withSuccess())

        builder
            .build()
            .get()
            .uri("https://downstream.test/resource")
            .retrieve()
            .toBodilessEntity()

        server.verify()
        verify(exactly = 1) { tokenX.hentToken(scope, "subject-test-token") }
    }

    @Test
    fun `feilet tokenutveksling sender ikke forespørsel videre`() {
        every { tokenX.hentToken("pdl-scope", "subject-test-token") } throws IllegalStateException("synthetic-exchange-error")
        val builder = config.pdlTokenXRestClient(tokenX, "pdl-scope").mutate()
        val server = MockRestServiceServer.bindTo(builder).build()

        assertThatThrownBy {
            builder
                .build()
                .get()
                .uri("https://downstream.test/resource")
                .retrieve()
                .toBodilessEntity()
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("synthetic-exchange-error")

        server.verify()
    }

    @ParameterizedTest
    @ValueSource(strings = ["pdl", "kodeverk"])
    fun `systemklienter bruker Entra-klient med konfigurert scope`(klient: String) {
        val scope = "$klient-system-scope"
        val expected = RestClient.create()
        every { entraFactory.lagMaskinTilMaskinRestKlient(scope) } returns expected

        val actual =
            if (klient == "pdl") {
                config.pdlClientCredentialRestClient(scope)
            } else {
                config.kodeverkRestClient(scope)
            }

        assertThat(actual).isSameAs(expected)
        verify(exactly = 1) { entraFactory.lagMaskinTilMaskinRestKlient(scope) }
    }
}
