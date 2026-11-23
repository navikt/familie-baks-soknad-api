package no.nav.familie.baks.soknad.api.kodeverk

import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.controllers.KodeverkController
import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.RestClient

class KodeverkHttpTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val kodeverkService = CachedKodeverkService(KodeverkClient("https://kodeverk.test", builder.build()))
    private val mvc = MockMvcBuilders.standaloneSetup(KodeverkController(kodeverkService)).build()

    @AfterEach
    fun verifiserForespørsler() {
        server.verify()
    }

    @ParameterizedTest
    @CsvSource("alle-land, Landkoder, USA, USA", "eos-land, EEAFreg, DNK, Danmark")
    fun `landruten leverer data fra riktig kodeverk`(
        rute: String,
        kodeverk: String,
        kode: String,
        term: String
    ) {
        forventKodeverk(kodeverk, kode, term)

        mvc.get("/api/kodeverk/$rute").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("SUKSESS") }
            jsonPath("$.data.$kode") { value(term) }
        }
    }

    @Test
    fun `postnummeroppslag leser poststed fra kodeverket`() {
        forventKodeverk("Postnummer", "0123", "Teststed")

        assertThat(kodeverkService.hentPostnummer()).containsExactlyEntriesOf(mapOf("0123" to "Teststed"))
    }

    private fun forventKodeverk(
        kodeverk: String,
        kode: String,
        term: String
    ) {
        server
            .expect(requestTo("https://kodeverk.test/api/v1/kodeverk/$kodeverk/koder/betydninger?ekskluderUgyldige=true&spraak=nb"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    """
                    {"betydninger":{"$kode":[{
                      "gyldigFra":"1900-01-01","gyldigTil":"9999-12-31","beskrivelser":{"nb":{"term":"$term","tekst":"$term"}}
                    }]}}
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )
    }
}
