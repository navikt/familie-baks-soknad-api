package no.nav.familie.baks.soknad.api.ratelimit

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.TimeMeter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Begrensning per bruker og pod, ikke en global kvote eller beskyttelse mot dobbeltinnsending.
 * Kvoten nullstilles når en pod starter på nytt eller en bruker fjernes fra cachen.
 */
@Component
class SøknadRateLimiter internal constructor(
    rateLimitingProperties: RateLimitingProperties,
    private val timeMeter: TimeMeter
) {
    @Autowired
    constructor(rateLimitingProperties: RateLimitingProperties) : this(rateLimitingProperties, TimeMeter.SYSTEM_NANOTIME)

    private val grense =
        Bandwidth
            .builder()
            .capacity(rateLimitingProperties.kapasitet)
            .refillGreedy(rateLimitingProperties.kapasitet, rateLimitingProperties.refillPeriode)
            .build()

    private val bøtter =
        Caffeine
            .newBuilder()
            .ticker { timeMeter.currentTimeNanos() }
            .expireAfterAccess(rateLimitingProperties.refillPeriode)
            .maximumSize(MAKS_ANTALL_SPOREDE_BRUKERE)
            .build<String, Bucket>()

    fun forsøkForbruk(nøkkel: String): Forbruksresultat {
        val bøtte = bøtter.get(nøkkel) { _ -> lagBøtte() }
        val probe: ConsumptionProbe = bøtte.tryConsumeAndReturnRemaining(1)
        return Forbruksresultat(
            tillatt = probe.isConsumed,
            sekunderTilNyttForsøk =
                Math.ceilDiv(probe.nanosToWaitForRefill, TimeUnit.SECONDS.toNanos(1)).coerceAtLeast(1)
        )
    }

    private fun lagBøtte(): Bucket =
        Bucket
            .builder()
            .addLimit(grense)
            .withCustomTimePrecision(timeMeter)
            .build()

    companion object {
        private const val MAKS_ANTALL_SPOREDE_BRUKERE = 100_000L
    }
}

data class Forbruksresultat(
    val tillatt: Boolean,
    val sekunderTilNyttForsøk: Long
)
