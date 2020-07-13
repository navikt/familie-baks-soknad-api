package no.nav.familie.ba.soknad.api.config

import no.nav.familie.ba.soknad.api.util.TokenBehandler
import no.nav.familie.http.interceptor.ApiKeyInjectingClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.http.sts.StsRestClient
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
import java.net.URI

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
    fun apiKeyInjectingClientInterceptor(@Value("\${PDL_API_APIKEY}") pdlApiKey: String,
                                         @Value("\${PDL_API_URL}") pdlBaseUrl: String): ClientHttpRequestInterceptor {
        val map = mapOf(Pair(URI.create(pdlBaseUrl), Pair(apiKeyHeader, pdlApiKey)))
        return ApiKeyInjectingClientInterceptor(map)
    }

    @Bean
    @Primary
    fun restTemplate(consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
    }

    @Bean
    fun jwtTokenInjectingInterceptor(): ClientHttpRequestInterceptor {
        return AddJwtTokenInterceptor()
    }

    @Bean
    fun stsTokenInjectingInterceptor(stsRestClient: StsRestClient): ClientHttpRequestInterceptor {
        return AddSTSAuthorizationInterceptor(stsRestClient)
    }

    @Bean("restKlientMedApiKey")
    fun restTemplateMedApiKey(consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor,
                              jwtTokenInjectingInterceptor: ClientHttpRequestInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                              apiKeyInjectingClientInterceptor,
                              jwtTokenInjectingInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
    }

    @Bean("ekspandertAutorisasjonRestKlientMedApiKey")
    fun expAuthRestTemplateMedApiKey(consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor,
                              stsTokenInjectingInterceptor: ClientHttpRequestInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                        apiKeyInjectingClientInterceptor,
                        stsTokenInjectingInterceptor,
                        MdcValuesPropagatingClientInterceptor())
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
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

class AddSTSAuthorizationInterceptor(private val stsRestClient: StsRestClient) : ClientHttpRequestInterceptor {
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers["Authorization"] = "Bearer ${stsRestClient.systemOIDCToken}"
        return execution.execute(request, body)
    }
}
