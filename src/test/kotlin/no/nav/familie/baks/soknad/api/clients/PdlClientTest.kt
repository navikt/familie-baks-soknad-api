package no.nav.familie.baks.soknad.api.clients

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.familie.baks.soknad.api.clients.pdl.PdlApp2AppClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlBrukerClient
import no.nav.familie.baks.soknad.api.clients.pdl.PdlExtensions
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.clients.pdl.PdlPersonRequest
import no.nav.familie.baks.soknad.api.clients.pdl.PdlWarning
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class PdlClientTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val client = PdlBrukerClient("https://pdl.test", builder.build())

    @AfterEach
    fun verifiserForespørsler() {
        server.verify()
    }

    @Test
    fun `personoppslag sender GraphQL med identitet og behandlingsgrunnlag`() {
        val response = requireNotNull(javaClass.getResource("/pdl/pdlPersonUtenRelasjoner.json")).readText()
        server
            .expect(requestTo("https://pdl.test/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Tema", Ytelse.BARNETRYGD.tema.name))
            .andExpect(header("behandlingsnummer", Ytelse.BARNETRYGD.tema.behandlingsnummer))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect { request ->
                val body = jsonMapper.readValue((request as MockClientHttpRequest).bodyAsString, PdlPersonRequest::class.java)
                assertThat(body.variables.ident).isEqualTo("test-subject")
                assertThat(body.query).contains("hentPerson(ident: ${'$'}ident)", "adressebeskyttelse", "forelderBarnRelasjon")
            }.andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val person = client.hentPerson("test-subject", Ytelse.BARNETRYGD)

        assertThat(person.data.person).isNotNull
        assertThat(person.harFeil()).isFalse
    }

    @Test
    fun `GraphQL-feil gir generisk feil og detaljer bare i sikker logger`() {
        val ordinaryLogger = LoggerFactory.getLogger(PdlApp2AppClient::class.java) as Logger
        val secureLogger = LoggerFactory.getLogger("secureLogger") as Logger
        val ordinary = ListAppender<ILoggingEvent>().apply { start() }
        val secure = ListAppender<ILoggingEvent>().apply { start() }
        ordinaryLogger.addAppender(ordinary)
        secureLogger.addAppender(secure)
        val markør = "synthetic-pdl-detail"
        server
            .expect(requestTo("https://pdl.test/graphql"))
            .andRespond(
                withSuccess(
                    """{"data":{"person":null},"errors":[{"message":"$markør","extensions":null}],"extensions":null}""",
                    MediaType.APPLICATION_JSON
                )
            )
        try {
            assertThatThrownBy { client.hentPerson("test-subject", Ytelse.KONTANTSTOTTE) }
                .hasMessage("En feil har oppstått ved henting av person")
            assertThat(ordinary.list.map { it.formattedMessage }).isNotEmpty.noneMatch { it.contains(markør) }
            assertThat(secure.list.map { it.formattedMessage }).anyMatch { it.contains(markør) }
        } finally {
            ordinaryLogger.detachAppender(ordinary)
            secureLogger.detachAppender(secure)
            ordinary.stop()
            secure.stop()
        }
    }

    @Test
    fun `advarsel i GraphQL-svar avviser ikke gyldige persondata`() {
        val person =
            requireNotNull(javaClass.getResourceAsStream("/pdl/pdlPersonUtenRelasjoner.json")).use {
                jsonMapper.readValue(it, PdlHentPersonResponse::class.java)
            }
        val medAdvarsel = person.copy(extensions = PdlExtensions(listOf(PdlWarning(null, "synthetic-warning", "synthetic-warning", null))))
        server
            .expect(requestTo("https://pdl.test/graphql"))
            .andRespond(withSuccess(jsonMapper.writeValueAsString(medAdvarsel), MediaType.APPLICATION_JSON))

        val response = client.hentPerson("test-subject", Ytelse.KONTANTSTOTTE)

        assertThat(response.data.person).isEqualTo(person.data.person)
        assertThat(response.harAdvarsel()).isTrue
    }
}
