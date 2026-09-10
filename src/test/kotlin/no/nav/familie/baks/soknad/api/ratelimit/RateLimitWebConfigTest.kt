package no.nav.familie.baks.soknad.api.ratelimit

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.handler.MappedInterceptor
import org.springframework.web.util.ServletRequestPathUtils

class RateLimitWebConfigTest {
    private val mappedInterceptor =
        object : InterceptorRegistry() {
            fun hentMappedInterceptor(): MappedInterceptor = interceptors.filterIsInstance<MappedInterceptor>().single()
        }.apply {
            RateLimitWebConfig(mockk<SøknadRateLimitInterceptor>()).addInterceptors(this)
        }.hentMappedInterceptor()

    @ParameterizedTest
    @ValueSource(strings = ["/api/soknad/v10", "/api/soknad/v9", "/api/soknad/kontantstotte/v6"])
    fun `skal kun registrere interceptor for de avtalte innsendingsendepunktene`(path: String) {
        assertTrue(mappedInterceptor.matches(request(path)))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/api/soknad",
            "/api/soknad/status",
            "/api/soknad/v11",
            "/api/soknad/v10/vedlegg",
            "/api/soknad/kontantstotte/v7",
            "/api/personopplysning",
            "/api/kodeverk/alle-land",
            "/api/kontoregister/hent-kontonr",
            "/api/innlogget/barnetrygd"
        ]
    )
    fun `skal ikke registrere interceptor for andre stier eller nye kontraktversjoner`(path: String) {
        assertFalse(mappedInterceptor.matches(request(path)))
    }

    private fun request(path: String): MockHttpServletRequest =
        MockHttpServletRequest("POST", path).also {
            ServletRequestPathUtils.parseAndCache(it)
        }
}
