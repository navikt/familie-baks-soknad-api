package no.nav.familie.baks.soknad.api.ratelimit

import io.github.bucket4j.TimeMeter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration
import kotlin.test.Test

class SøknadRateLimiterTest {
    private var nanos = 0L
    private val timeMeter =
        object : TimeMeter {
            override fun currentTimeNanos(): Long = nanos

            override fun isWallClockBased(): Boolean = false
        }
    private val kapasitet = 3L
    private val rateLimiter =
        SøknadRateLimiter(
            RateLimitingProperties(
                enabled = true,
                kapasitet = kapasitet,
                refillPeriode = Duration.ofMinutes(1)
            ),
            timeMeter
        )

    @Test
    fun `skal tillate forbruk opp til kapasiteten og deretter avvise`() {
        val fnr = "12345678901"

        repeat(kapasitet.toInt()) {
            assertTrue(rateLimiter.forsøkForbruk(fnr).tillatt)
        }

        val resultat = rateLimiter.forsøkForbruk(fnr)
        assertFalse(resultat.tillatt)
        assertTrue(resultat.sekunderTilNyttForsøk >= 1)
    }

    @Test
    fun `skal telle kvote separat per bruker`() {
        val førsteBruker = "11111111111"
        val andreBruker = "22222222222"

        repeat(kapasitet.toInt()) { rateLimiter.forsøkForbruk(førsteBruker) }

        assertFalse(rateLimiter.forsøkForbruk(førsteBruker).tillatt)
        assertTrue(rateLimiter.forsøkForbruk(andreBruker).tillatt)
    }

    @ParameterizedTest
    @ValueSource(longs = [0, 1, 100_000_000, 19_999_999_999])
    fun `skal runde ventetid opp og tillate et nytt kall etter oppgitt ventetid`(forløptTid: Long) {
        repeat(kapasitet.toInt()) { rateLimiter.forsøkForbruk("bruker") }
        nanos += forløptTid

        val resultat = rateLimiter.forsøkForbruk("bruker")
        assertFalse(resultat.tillatt)
        assertEquals(if (forløptTid == 19_999_999_999L) 1L else 20L, resultat.sekunderTilNyttForsøk)

        nanos += Duration.ofSeconds(resultat.sekunderTilNyttForsøk).toNanos()
        assertTrue(rateLimiter.forsøkForbruk("bruker").tillatt)
    }

    @Test
    fun `skal beholde kvoten også når refill-perioden er kortere enn ett sekund`() {
        val limiter = SøknadRateLimiter(RateLimitingProperties(kapasitet = 1, refillPeriode = Duration.ofMillis(100)), timeMeter)

        assertTrue(limiter.forsøkForbruk("bruker").tillatt)
        assertFalse(limiter.forsøkForbruk("bruker").tillatt)
        nanos += Duration.ofMillis(100).toNanos()
        assertTrue(limiter.forsøkForbruk("bruker").tillatt)
    }

    @Test
    fun `skal ha full kvote etter en inaktiv refill-periode`() {
        repeat(kapasitet.toInt()) { rateLimiter.forsøkForbruk("bruker") }
        nanos += Duration.ofMinutes(1).toNanos()

        repeat(kapasitet.toInt()) { assertTrue(rateLimiter.forsøkForbruk("bruker").tillatt) }
        assertFalse(rateLimiter.forsøkForbruk("bruker").tillatt)
    }
}
