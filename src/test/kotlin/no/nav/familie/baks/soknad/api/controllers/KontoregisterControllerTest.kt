package no.nav.familie.baks.soknad.api.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.baks.soknad.api.clients.kontoregister.KontoregisterClient
import no.nav.familie.baks.soknad.api.clients.kontoregister.KontoregisterResponseDto
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

class KontoregisterControllerTest {
    private val client = mockk<KontoregisterClient>()
    private val controller = KontoregisterController(client)

    @BeforeEach
    fun settOpp() {
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns "token-subject"
    }

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @Test
    fun `kontooppslag bruker tokenidentitet og returnerer funnet konto`() {
        every { client.hentKontonummer("token-subject") } returns KontoregisterResponseDto("synthetic-account")

        val response = controller.hentKontoForInnloggetBruker()

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.kontonummer).isEqualTo("synthetic-account")
        verify(exactly = 1) { client.hentKontonummer("token-subject") }
    }

    @Test
    fun `tom konto gir feilressurs uten kontodata`() {
        every { client.hentKontonummer("token-subject") } returns KontoregisterResponseDto("")

        val response = controller.hentKontoForInnloggetBruker()

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).isNull()
        assertThat(response.body?.melding).isEqualTo("Klarte ikke finne kontonummer")
    }

    @Test
    fun `konto som ikke finnes gir 404 uten interne detaljer`() {
        every { client.hentKontonummer("token-subject") } throws
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "synthetic-error", HttpHeaders.EMPTY, byteArrayOf(), null)

        val response = controller.hentKontoForInnloggetBruker()

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.melding).isEqualTo("Klarte ikke finne kontonummer")
    }

    @Test
    fun `driftsfeil blir ikke behandlet som manglende konto`() {
        every { client.hentKontonummer("token-subject") } throws HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE)

        assertThatThrownBy { controller.hentKontoForInnloggetBruker() }.isInstanceOf(HttpServerErrorException::class.java)
    }
}
