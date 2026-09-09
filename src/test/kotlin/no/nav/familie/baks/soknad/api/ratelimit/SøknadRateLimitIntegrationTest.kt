package no.nav.familie.baks.soknad.api.ratelimit

import no.nav.familie.baks.soknad.api.MockOAuth2ServerConfig
import no.nav.familie.baks.soknad.api.config.ApplicationConfig
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.client.RestClient
import java.time.LocalDateTime

@SpringBootTest(
    classes = [ApplicationConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["rate-limiting.soknad.kapasitet=2", "rate-limiting.soknad.refill-periode=1h"]
)
class SøknadRateLimitIntegrationTest : MockOAuth2ServerConfig() {
    @LocalServerPort
    private var port: Int = 0

    @MockitoBean
    private lateinit var barnetrygdSøknadService: BarnetrygdSøknadService

    private val restClient =
        RestClient
            .builder()
            .requestFactory(JdkClientHttpRequestFactory())
            .defaultStatusHandler(HttpStatusCode::isError) { _, _ -> }
            .build()

    @Test
    fun `skal autentisere før kvotesjekk og dele kvoten mellom innsendingsendepunktene`() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        `when`(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
            .thenReturn(Ressurs.success(Kvittering("OK", LocalDateTime.now())))
        val token = lagTestToken()

        fun sendInn(
            path: String,
            bearerToken: String?
        ) = restClient
            .post()
            .uri("http://localhost:$port$path")
            .apply { bearerToken?.let { header(HttpHeaders.AUTHORIZATION, "Bearer $it") } }
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonMapper.writeValueAsString(søknad))
            .retrieve()
            .toEntity(String::class.java)

        assertThat(sendInn("/api/soknad/v10", null).statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        repeat(2) {
            assertThat(sendInn("/api/soknad/v10", token).statusCode).isEqualTo(HttpStatus.OK)
        }
        listOf("/api/soknad/v10", "/api/soknad/v9", "/api/soknad/kontantstotte/v6").forEach { path ->
            val avvist = sendInn(path, token)
            assertThat(avvist.statusCode).isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            assertThat(avvist.headers.getFirst(HttpHeaders.RETRY_AFTER)?.toLong()).isPositive()
            assertThat(jsonMapper.readTree(avvist.body)["status"].asText()).isEqualTo("FEILET")
        }
        assertThat(sendInn("/api/soknad/v10", null).statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
