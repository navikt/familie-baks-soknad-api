package no.nav.familie.ba.soknad.api.config

import java.net.URI
import no.nav.familie.ba.soknad.api.util.TokenBehandler
import no.nav.familie.http.interceptor.ApiKeyInjectingClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.filter.LogFilter
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
@Import(ConsumerIdClientInterceptor::class)
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
    fun apiKeyInjectingClientInterceptor(
        @Value("\${PDL_API_APIKEY}") pdlApiKey: String,
        @Value("\${PDL_API_URL}") pdlBaseUrl: String,
        @Value("\${MOTTAK_APIKEY}") mottakApiKey: String,
        @Value("\${FAMILIE_BA_MOTTAK_URL}") mottakBaseUrl: String,
        // @Value("\${KODEVERK_API_KEY}") kodeverkApiKey: String,
        // @Value("\${KODEVERK_URL}") kodeverkBaseUrl: String,
    ): ClientHttpRequestInterceptor {
        val map = mapOf(
            Pair(URI.create(pdlBaseUrl), Pair(apiKeyHeader, pdlApiKey)),
            Pair(URI.create(mottakBaseUrl), Pair(apiKeyHeader, mottakApiKey)),
            //  Pair(URI.create(kodeverkBaseUrl), Pair(apiKeyHeader, kodeverkApiKey))
        )
        return ApiKeyInjectingClientInterceptor(map)
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

    @Bean("restKlientMedApiKeyOgBrukerToken")
    fun restTemplateMedApiKey(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor,
        jwtTokenInjectingInterceptor: ClientHttpRequestInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                apiKeyInjectingClientInterceptor,
                jwtTokenInjectingInterceptor,
                MdcValuesPropagatingClientInterceptor()
            )
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Bean("restKlientMedApiKey")
    fun stsRestTemplateMedApiKey(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                apiKeyInjectingClientInterceptor,
                MdcValuesPropagatingClientInterceptor()
            )
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Bean("restKlientMottak")
    fun restTemplateMottak(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor,
        jwtTokenInjectingInterceptor: ClientHttpRequestInterceptor
    ): RestOperations {
        return RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                apiKeyInjectingClientInterceptor,
                jwtTokenInjectingInterceptor,
                MdcValuesPropagatingClientInterceptor()
            )
            .build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        const val pakkenavn = "no.nav.familie.ba.soknad.api"
        private const val apiKeyHeader = "x-nav-apiKey"
    }
}

class AddJwtTokenInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers["Authorization"] = "Bearer ${TokenBehandler.hentToken()}"
        return execution.execute(request, body)
    }
}
