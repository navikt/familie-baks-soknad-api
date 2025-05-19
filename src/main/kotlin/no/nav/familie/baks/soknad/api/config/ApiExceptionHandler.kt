package no.nav.familie.baks.soknad.api.config

import no.nav.familie.baks.soknad.api.common.GradertAdresseException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Ressurs<Nothing>> {
        val feilmelding = (throwable.cause?.message ?: throwable.message).toString()

        if (throwable !is JwtTokenUnauthorizedException) {
            secureLogger.info("En feil har oppstått: $feilmelding", throwable)
            LOG.error("En feil har oppstått: $feilmelding")
        }

        return when (throwable) {
            is HttpClientErrorException -> ResponseEntity.status(throwable.statusCode).body(Ressurs.failure(feilmelding))
            is JwtTokenUnauthorizedException -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Ressurs.failure(feilmelding))
            is IllegalArgumentException -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Ressurs.failure(feilmelding))
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Ressurs.failure(feilmelding))
        }
    }

    @ExceptionHandler(GradertAdresseException::class)
    fun handleGradertAdresseException(gradertAdresseException: GradertAdresseException): ResponseEntity<Ressurs<String>> {
        secureLogger.info("Spørring for person med gradert adresse avvist")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Ressurs.ikkeTilgang("Ikke tilgang"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(illegalArgumentException: IllegalArgumentException): ResponseEntity<Ressurs<String>> {
        val feilmelding = (illegalArgumentException.cause?.message ?: illegalArgumentException.message).toString()
        secureLogger.info("Validering av søknad feilet", illegalArgumentException)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Ressurs.failure(feilmelding))
    }

    /**
     * AsyncRequestNotUsableException er en exception som blir kastet når en async request blir avbrutt. Velger
     * å skjule denne exceptionen fra loggen da den ikke er interessant for oss.
     */
    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handlAsyncRequestNotUsableException(e: AsyncRequestNotUsableException): ResponseEntity<Any> {
        LOG.info("En AsyncRequestNotUsableException har oppstått, som skjer når en async request blir avbrutt", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
