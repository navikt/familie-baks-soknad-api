package no.nav.familie.baks.soknad.api.config

import no.nav.familie.http.interceptor.BearerTokenClientCredentialsClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenExchangeClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ComponentScan(ApplicationConfig.PAKKENAVN)
@Import(
    MdcValuesPropagatingClientInterceptor::class,
    ConsumerIdClientInterceptor::class,
    BearerTokenExchangeClientInterceptor::class,
    BearerTokenClientCredentialsClientInterceptor::class
)
@EnableOAuth2Client(cacheEnabled = true)
internal class ApplicationConfig {
    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.filter = LogFilter(NavSystemtype.NAV_EKSTERN_BRUKERFLATE)
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    @Primary
    fun restTemplate(consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations =
        RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                MdcValuesPropagatingClientInterceptor()
            ).additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()

    @Bean("clientCredential")
    fun clientCredentialRestTemplateMedApiKey(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientCredentialsClientInterceptor: BearerTokenClientCredentialsClientInterceptor,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .interceptors(
                consumerIdClientInterceptor,
                bearerTokenClientCredentialsClientInterceptor,
                mdcValuesPropagatingClientInterceptor
            ).build()

    @Bean("tokenExchange")
    fun tokenExchangeRestTemplate(
        bearerTokenExchangeClientInterceptor: BearerTokenExchangeClientInterceptor,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .interceptors(
                bearerTokenExchangeClientInterceptor,
                mdcValuesPropagatingClientInterceptor,
                consumerIdClientInterceptor
            ).build()

    companion object {
        private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)
        const val PAKKENAVN = "no.nav.familie.baks.soknad.api"
    }
}
