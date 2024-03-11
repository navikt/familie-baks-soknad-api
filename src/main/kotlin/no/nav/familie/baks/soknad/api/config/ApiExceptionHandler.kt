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
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Ressurs.failure(feilmelding))
        }
    }

    @ExceptionHandler(GradertAdresseException::class)
    fun handleGradertAdresseException(gradertAdresseException: GradertAdresseException): ResponseEntity<Ressurs<String>> {
        secureLogger.info("Spørring for person med gradert adresse avvist")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Ressurs.ikkeTilgang("Ikke tilgang"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
