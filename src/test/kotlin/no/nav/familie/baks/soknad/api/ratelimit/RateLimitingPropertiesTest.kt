package no.nav.familie.baks.soknad.api.ratelimit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Import
import java.time.Duration

class RateLimitingPropertiesTest {
    private val contextRunner = ApplicationContextRunner().withUserConfiguration(TestConfig::class.java)

    @ParameterizedTest
    @ValueSource(strings = ["kapasitet=0", "kapasitet=-1", "refill-periode=0s", "refill-periode=-1s"])
    fun `skal stoppe oppstart ved ugyldig konfigurasjon`(egenskap: String) {
        contextRunner
            .withPropertyValues("rate-limiting.soknad.$egenskap")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure)
                    .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
                    .hasStackTraceContaining("rate-limiting.soknad.${egenskap.substringBefore('=')}")
            }
    }

    @Test
    fun `skal binde gyldig konfigurasjon og opprette limiter ved oppstart`() {
        contextRunner
            .withPropertyValues(
                "rate-limiting.soknad.enabled=false",
                "rate-limiting.soknad.kapasitet=2",
                "rate-limiting.soknad.refill-periode=30s"
            ).run { context ->
                assertThat(context).hasNotFailed().hasSingleBean(SøknadRateLimiter::class.java)
                assertThat(context.getBean(RateLimitingProperties::class.java))
                    .isEqualTo(RateLimitingProperties(enabled = false, kapasitet = 2, refillPeriode = Duration.ofSeconds(30)))
            }
    }

    @EnableConfigurationProperties(RateLimitingProperties::class)
    @Import(SøknadRateLimiter::class)
    class TestConfig
}
