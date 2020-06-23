package no.nav.familie.ba.soknad.api.config

import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Ressurs<Nothing>> {
        val feilmelding = (throwable.cause?.message ?: throwable.message).toString()
        secureLogger.info("En feil har oppstått: $feilmelding", throwable)
        LOG.error("En feil har oppstått: $feilmelding")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Ressurs.failure(feilmelding))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}