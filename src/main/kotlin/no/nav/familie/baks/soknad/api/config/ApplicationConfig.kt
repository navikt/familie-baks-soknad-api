package no.nav.familie.baks.soknad.api.config

import java.net.URI
import java.time.Duration
import java.time.temporal.ChronoUnit
import no.nav.familie.baks.soknad.api.util.TokenBehandler
import no.nav.familie.http.interceptor.ApiKeyInjectingClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenClientCredentialsClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenExchangeClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations

@SpringBootConfiguration
@ComponentScan(ApplicationConfig.pakkenavn)
@Import(
    MdcValuesPropagatingClientInterceptor::class,
    ConsumerIdClientInterceptor::class,
    BearerTokenExchangeClientInterceptor::class,
    BearerTokenClientCredentialsClientInterceptor::class,
    BearerTokenClientInterceptor::class
)
@EnableOAuth2Client(cacheEnabled = true)
internal class ApplicationConfig {

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        log.info("Registering LogFilter filter")
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    @Primary
    fun restTemplate(consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                MdcValuesPropagatingClientInterceptor()
            )
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Bean
    fun jwtTokenInjectingInterceptor(): ClientHttpRequestInterceptor {
        return AddJwtTokenInterceptor()
    }

    @Bean("restKlientMottak")
    fun restTemplateMottak(
        bearerTokenClientInterceptor: BearerTokenClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .interceptors(
                bearerTokenClientInterceptor,
                consumerIdClientInterceptor,
                MdcValuesPropagatingClientInterceptor()
            )
            .build()
    }

    @Bean("clientCredential")
    fun clientCredentialRestTemplateMedApiKey(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientCredentialsClientInterceptor: BearerTokenClientCredentialsClientInterceptor,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .setConnectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .interceptors(
                consumerIdClientInterceptor,
                bearerTokenClientCredentialsClientInterceptor,
                mdcValuesPropagatingClientInterceptor
            )
            .build()
    }

    @Bean("tokenExchange")
    fun restTemplate(
        bearerTokenExchangeClientInterceptor: BearerTokenExchangeClientInterceptor,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .setConnectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .interceptors(
                bearerTokenExchangeClientInterceptor,
                mdcValuesPropagatingClientInterceptor,
                consumerIdClientInterceptor
            )
            .build()
    }

    companion object {

        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        const val pakkenavn = "no.nav.familie.baks.soknad.api"
        private const val apiKeyHeader = "x-nav-apiKey"
    }
}

class AddJwtTokenInterceptor : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers["Authorization"] = "Bearer ${TokenBehandler.hentToken()}"
        return execution.execute(request, body)
    }
}