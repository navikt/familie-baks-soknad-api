package no.nav.familie.baks.soknad.api.helsesjekk

import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.config.ApiExceptionHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.RestClient

class HelsesjekkHttpTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val client = builder.build()
    private val mvc =
        MockMvcBuilders
            .standaloneSetup(
                HelsesjekkController(
                    MottakClient("https://mottak.test", client),
                    PdlApp2AppClient("https://pdl.test", client),
                    KodeverkClient("https://kodeverk.test", client)
                )
            ).setControllerAdvice(ApiExceptionHandler())
            .build()

    @AfterEach
    fun verifiserForespørsler() {
        server.verify()
    }

    @ParameterizedTest
    @EnumSource(Avhengighet::class)
    fun `helsesjekk rapporterer suksess når avhengigheten svarer`(avhengighet: Avhengighet) {
        forventPing(avhengighet, HttpStatus.NO_CONTENT)

        mvc.get("/api/helse/${avhengighet.rute}").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("SUKSESS") }
        }
    }

    @ParameterizedTest
    @EnumSource(Avhengighet::class)
    fun `helsesjekk rapporterer feil når avhengigheten er utilgjengelig`(avhengighet: Avhengighet) {
        forventPing(avhengighet, HttpStatus.SERVICE_UNAVAILABLE)

        mvc.get("/api/helse/${avhengighet.rute}").andExpect {
            status { is5xxServerError() }
            jsonPath("$.status") { value("FEILET") }
        }
    }

    private fun forventPing(
        avhengighet: Avhengighet,
        status: HttpStatus
    ) {
        server
            .expect(requestTo(avhengighet.url))
            .andExpect(method(avhengighet.metode))
            .andRespond(withStatus(status))
    }

    enum class Avhengighet(
        val rute: String,
        val url: String,
        val metode: HttpMethod
    ) {
        PDL("pdl", "https://pdl.test/graphql", HttpMethod.OPTIONS),
        MOTTAK("mottak", "https://mottak.test/api/soknad", HttpMethod.OPTIONS),
        KODEVERK("kodeverk", "https://kodeverk.test/internal/isAlive", HttpMethod.GET)
    }
}
