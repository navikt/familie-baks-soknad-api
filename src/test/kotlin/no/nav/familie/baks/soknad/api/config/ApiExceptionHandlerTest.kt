package no.nav.familie.baks.soknad.api.config

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.familie.baks.soknad.api.common.GradertAdresseException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()

    @Test
    fun `HTTP-klientfeil beholder status mens uventet feil gir 500`() {
        val klientfeil = handler.handleThrowable(HttpClientErrorException(HttpStatus.BAD_REQUEST, "synthetic-error"))
        val uventetFeil = handler.handleThrowable(IllegalStateException("synthetic-error"))

        assertThat(klientfeil.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertNull(klientfeil.body?.data)
        assertThat(uventetFeil.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertNull(uventetFeil.body?.data)
    }

    @Test
    fun `valideringsfeil gir 400 uten data`() {
        val response = handler.handleIllegalArgumentException(IllegalArgumentException("synthetic-error"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.data).isNull()
    }

    @Test
    fun `gradert adresse gir generisk 403 og logges bare sikkert`() {
        val ordinaryLogger = LoggerFactory.getLogger(ApiExceptionHandler::class.java) as Logger
        val secureLogger = LoggerFactory.getLogger("secureLogger") as Logger
        val ordinary = ListAppender<ILoggingEvent>().apply { start() }
        val secure = ListAppender<ILoggingEvent>().apply { start() }
        ordinaryLogger.addAppender(ordinary)
        secureLogger.addAppender(secure)
        try {
            val response = handler.handleGradertAdresseException(GradertAdresseException())

            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            assertThat(response.body?.melding).isEqualTo("Ikke tilgang")
            assertThat(response.body?.data).isNull()
            assertThat(ordinary.list).isEmpty()
            assertThat(secure.list.map { it.formattedMessage }).containsExactly("Spørring for person med gradert adresse avvist")
        } finally {
            ordinaryLogger.detachAppender(ordinary)
            secureLogger.detachAppender(secure)
            ordinary.stop()
            secure.stop()
        }
    }
}
