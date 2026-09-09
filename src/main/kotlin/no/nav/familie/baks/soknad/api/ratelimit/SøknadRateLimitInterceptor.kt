package no.nav.familie.baks.soknad.api.ratelimit

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SøknadRateLimitInterceptor(
    private val rateLimitingProperties: RateLimitingProperties,
    private val søknadRateLimiter: SøknadRateLimiter
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (!rateLimitingProperties.enabled || request.method != HttpMethod.POST.name()) {
            return true
        }

        val fnr = EksternBrukerUtils.hentFnrFraToken()
        val resultat = søknadRateLimiter.forsøkForbruk(fnr)
        if (resultat.tillatt) {
            return true
        }

        logger.warn("Avviste søknadsinnsending på grunn av rate-limiting (429)")
        skrivForMangeForespørsler(response, resultat.sekunderTilNyttForsøk)
        return false
    }

    private fun skrivForMangeForespørsler(
        response: HttpServletResponse,
        sekunderTilNyttForsøk: Long
    ) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.setHeader(HttpHeaders.RETRY_AFTER, sekunderTilNyttForsøk.toString())
        response.writer.write(
            jsonMapper.writeValueAsString(
                Ressurs.failure<String>("Du har sendt for mange forespørsler. Vent litt og prøv igjen senere.")
            )
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadRateLimitInterceptor::class.java)
    }
}
