package no.nav.familie.baks.soknad.api.clients

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadTestData
import no.nav.familie.kontrakter.felles.Ressurs
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
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9

class MottakClientTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val client = MottakClient("https://mottak.test", builder.build())
    private val kvittering = Kvittering("OK", LocalDateTime.of(2026, 6, 15, 12, 0))

    @AfterEach
    fun verifiserForespørsler() {
        server.verify()
    }

    @ParameterizedTest
    @EnumSource(Ytelse::class)
    fun `søknad sendes som JSON-del i multipart og kvittering returneres`(ytelse: Ytelse) {
        val søknad =
            when (ytelse) {
                Ytelse.BARNETRYGD -> BarnetrygdSøknadTestData.barnetrygdSøknad()
                Ytelse.KONTANTSTOTTE -> KontantstøtteSøknadTestData.kontantstøtteSøknad()
            }
        val path = if (ytelse == Ytelse.BARNETRYGD) "/api/soknad/v10" else "/api/kontantstotte/soknad/v6"
        forventSøknad(path, søknad)

        val resultat =
            when (ytelse) {
                Ytelse.BARNETRYGD -> client.sendBarnetrygdSøknad(BarnetrygdSøknadTestData.barnetrygdSøknad())
                Ytelse.KONTANTSTOTTE -> client.sendKontantstøtteSøknad(KontantstøtteSøknadTestData.kontantstøtteSøknad())
            }

        assertThat(resultat.data).isEqualTo(kvittering)
    }

    @Test
    fun `barnetrygd v9 beholder kontraktversjon og URL`() {
        val søknad = jsonMapper.convertValue(BarnetrygdSøknadTestData.barnetrygdSøknad(), BarnetrygdSøknadV9::class.java).copy(kontraktVersjon = 9)
        forventSøknad("/api/soknad/v9", søknad)

        assertThat(client.sendBarnetrygdSøknad(søknad).data).isEqualTo(kvittering)
    }

    @Test
    fun `mottaksfeil gir feil med opprinnelig årsak og ikke kvittering`() {
        server.expect(requestTo("https://mottak.test/api/soknad/v10")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        assertThatThrownBy { client.sendBarnetrygdSøknad(BarnetrygdSøknadTestData.barnetrygdSøknad()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Sende søknad til familie-baks-mottak feilet")
            .hasCauseInstanceOf(HttpServerErrorException::class.java)
    }

    @Test
    fun `timeout gir feil og ikke en vellykket kvittering`() {
        server.expect(requestTo("https://mottak.test/api/soknad/v10")).andRespond(withException(SocketTimeoutException("synthetic-timeout")))

        assertThatThrownBy { client.sendBarnetrygdSøknad(BarnetrygdSøknadTestData.barnetrygdSøknad()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasCauseInstanceOf(ResourceAccessException::class.java)
            .hasRootCauseInstanceOf(SocketTimeoutException::class.java)
    }

    private fun forventSøknad(
        path: String,
        søknad: Any
    ) {
        server
            .expect(requestTo("https://mottak.test$path"))
            .andExpect(method(HttpMethod.POST))
            .andExpect { request ->
                assertThat(request.headers.contentType?.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)).isTrue
                val body = (request as MockClientHttpRequest).bodyAsString
                assertThat(body).contains("name=\"søknad\"", "Content-Type: application/json")
                val json = body.substringAfter("\r\n\r\n").substringBeforeLast("\r\n--")
                assertThat(jsonMapper.readTree(json)).isEqualTo(jsonMapper.readTree(jsonMapper.writeValueAsString(søknad)))
            }.andRespond(withSuccess(jsonMapper.writeValueAsString(Ressurs.success(kvittering)), MediaType.APPLICATION_JSON))
    }
}
