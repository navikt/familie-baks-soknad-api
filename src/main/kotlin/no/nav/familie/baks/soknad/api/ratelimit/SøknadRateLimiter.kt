package no.nav.familie.baks.soknad.api.ratelimit

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Per-bruker (fnr) rate-limiter for søknadsinnsending.
 *
 * Bruker Bucket4j (token bucket) med Caffeine som in-memory lager. Merk at grensen dermed gjelder
 * per pod: med N replicas blir effektiv grense ~ N * [RateLimitingProperties.kapasitet]. Det er godt
 * nok for å hindre spam/doble innsendinger. Ved behov for eksakt global grense kan Bucket4j-backend
 * byttes til Valkey/Redis.
 */
@Component
class SøknadRateLimiter(
    private val rateLimitingProperties: RateLimitingProperties
) {
    private val bøtter =
        Caffeine
            .newBuilder()
            .expireAfterAccess(
                rateLimitingProperties.refillPeriode.multipliedBy(2).toSeconds(),
                TimeUnit.SECONDS
            ).maximumSize(MAKS_ANTALL_SPOREDE_BRUKERE)
            .build<String, Bucket>()

    fun forsøkForbruk(nøkkel: String): Forbruksresultat {
        val bøtte = bøtter.get(nøkkel) { _ -> lagBøtte() }
        val probe: ConsumptionProbe = bøtte.tryConsumeAndReturnRemaining(1)
        return Forbruksresultat(
            tillatt = probe.isConsumed,
            sekunderTilNyttForsøk =
                Duration
                    .ofNanos(probe.nanosToWaitForRefill)
                    .toSeconds()
                    .coerceAtLeast(1)
        )
    }

    private fun lagBøtte(): Bucket {
        val grense =
            Bandwidth
                .builder()
                .capacity(rateLimitingProperties.kapasitet)
                .refillGreedy(rateLimitingProperties.kapasitet, rateLimitingProperties.refillPeriode)
                .build()
        return Bucket.builder().addLimit(grense).build()
    }

    companion object {
        private const val MAKS_ANTALL_SPOREDE_BRUKERE = 100_000L
    }
}

data class Forbruksresultat(
    val tillatt: Boolean,
    val sekunderTilNyttForsøk: Long
)
