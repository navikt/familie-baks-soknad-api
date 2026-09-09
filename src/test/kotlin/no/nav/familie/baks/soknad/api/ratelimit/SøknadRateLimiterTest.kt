package no.nav.familie.baks.soknad.api.ratelimit

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Duration
import kotlin.test.Test

class SøknadRateLimiterTest {
    private val kapasitet = 3L
    private val rateLimiter =
        SøknadRateLimiter(
            RateLimitingProperties(
                enabled = true,
                kapasitet = kapasitet,
                refillPeriode = Duration.ofMinutes(1)
            )
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
}
