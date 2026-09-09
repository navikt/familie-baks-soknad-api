package no.nav.familie.baks.soknad.api.ratelimit

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class SøknadRateLimitInterceptorTest {
    private val limiter = mockk<SøknadRateLimiter>()
    private val interceptor = SøknadRateLimitInterceptor(RateLimitingProperties(), limiter)
    private val request = MockHttpServletRequest("POST", "/api/soknad/v10")
    private val response = MockHttpServletResponse()

    @BeforeEach
    fun settOpp() {
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns "testbruker"
    }

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @Test
    fun `skal returnere 429 med ventetid og ressurs ved tom kvote`() {
        every { limiter.forsøkForbruk("testbruker") } returns Forbruksresultat(false, 6)

        assertFalse(interceptor.preHandle(request, response, Any()))
        assertEquals(429, response.status)
        assertEquals("6", response.getHeader(HttpHeaders.RETRY_AFTER))
        assertEquals("UTF-8", response.characterEncoding)
        val body = jsonMapper.readTree(response.contentAsString)
        assertEquals("FEILET", body["status"].asText())
        assertEquals("Du har sendt for mange forespørsler. Vent litt og prøv igjen senere.", body["melding"].asText())
    }

    @Test
    fun `skal slippe gjennom kall med kvote`() {
        every { limiter.forsøkForbruk("testbruker") } returns Forbruksresultat(true, 1)

        assertTrue(interceptor.preHandle(request, response, Any()))
        assertEquals(200, response.status)
        assertTrue(response.contentAsString.isEmpty())
    }

    @Test
    fun `skal ikke bruke kvote naar limiter er deaktivert`() {
        val deaktivert = SøknadRateLimitInterceptor(RateLimitingProperties(enabled = false), limiter)

        assertTrue(deaktivert.preHandle(request, response, Any()))
        verify(exactly = 0) { limiter.forsøkForbruk(any()) }
        verify(exactly = 0) { EksternBrukerUtils.hentFnrFraToken() }
    }

    @Test
    fun `skal ikke bruke kvote for andre HTTP-metoder`() {
        request.method = "GET"

        assertTrue(interceptor.preHandle(request, response, Any()))
        verify(exactly = 0) { limiter.forsøkForbruk(any()) }
        verify(exactly = 0) { EksternBrukerUtils.hentFnrFraToken() }
    }

    @Test
    fun `skal propagere feil ved uthenting av ident`() {
        val feil = IllegalStateException("Mangler ident")
        every { EksternBrukerUtils.hentFnrFraToken() } throws feil

        assertSame(feil, assertThrows<IllegalStateException> { interceptor.preHandle(request, response, Any()) })
        verify(exactly = 0) { limiter.forsøkForbruk(any()) }
    }

    @Test
    fun `skal propagere uventede feil fra limiter`() {
        val feil = IllegalStateException("Limiter feilet")
        every { limiter.forsøkForbruk("testbruker") } throws feil

        assertSame(feil, assertThrows<IllegalStateException> { interceptor.preHandle(request, response, Any()) })
    }
}
