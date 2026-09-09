package no.nav.familie.baks.soknad.api.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "rate-limiting.soknad")
data class RateLimitingProperties(
    val enabled: Boolean = true,
    val kapasitet: Long = 10,
    val refillPeriode: Duration = Duration.ofMinutes(1)
)
