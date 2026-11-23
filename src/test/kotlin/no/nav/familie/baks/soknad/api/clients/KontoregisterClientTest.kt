package no.nav.familie.baks.soknad.api.clients

import no.nav.familie.baks.soknad.api.clients.kontoregister.KontoregisterClient
import no.nav.familie.baks.soknad.api.clients.kontoregister.KontoregisterRequestDto
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

class KontoregisterClientTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val client = KontoregisterClient("https://konto.test", builder.build())

    @AfterEach
    fun verifiserForespørsler() {
        server.verify()
    }

    @Test
    fun `kontooppslag sender kontohaver i JSON og leser kontonummer`() {
        server
            .expect(requestTo("https://konto.test/hent-aktiv-konto"))
            .andExpect(method(HttpMethod.POST))
            .andExpect { request ->
                assertThat(request.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
                val body = jsonMapper.readValue((request as MockClientHttpRequest).bodyAsString, KontoregisterRequestDto::class.java)
                assertThat(body.kontohaver).isEqualTo("test-subject")
            }.andRespond(withSuccess("""{"kontonummer":"synthetic-account"}""", MediaType.APPLICATION_JSON))

        assertThat(client.hentKontonummer("test-subject").kontonummer).isEqualTo("synthetic-account")
    }

    @ParameterizedTest
    @EnumSource(HttpStatus::class, names = ["NOT_FOUND", "UNAUTHORIZED", "SERVICE_UNAVAILABLE"])
    fun `klientfeil beholdes som feil og blir ikke tom konto`(status: HttpStatus) {
        server.expect(requestTo("https://konto.test/hent-aktiv-konto")).andRespond(withStatus(status))

        assertThatThrownBy { client.hentKontonummer("test-subject") }
            .isInstanceOfSatisfying(RestClientResponseException::class.java) {
                assertThat(it.statusCode).isEqualTo(status)
            }
    }
}
