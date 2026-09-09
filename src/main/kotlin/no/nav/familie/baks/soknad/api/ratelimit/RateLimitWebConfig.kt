package no.nav.familie.baks.soknad.api.ratelimit

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(RateLimitingProperties::class)
class RateLimitWebConfig(
    private val søknadRateLimitInterceptor: SøknadRateLimitInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(søknadRateLimitInterceptor)
            .addPathPatterns("/api/soknad/**")
    }
}
