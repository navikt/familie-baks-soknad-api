package no.nav.familie.ba.soknad.api.config

import no.nav.familie.http.interceptor.ApiKeyInjectingClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
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
        val apiKeyPdlMap = mapOf(Pair(URI.create(pdlBaseUrl), Pair(apiKeyHeader, pdlApiKey)))
        return ApiKeyInjectingClientInterceptor(apiKeyPdlMap)
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
    fun testInterceptor(@Value("\${PDL_API_APIKEY}") pdlApiKey: String,
                                         @Value("\${PDL_API_URL}") pdlBaseUrl: String): ClientHttpRequestInterceptor {
        return TestInterceptor()
    }

    @Bean("restKlientMedApiKey")
    fun restTemplateMedApiKey(consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              apiKeyInjectingClientInterceptor: ClientHttpRequestInterceptor,
                                testInterceptor: ClientHttpRequestInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                              apiKeyInjectingClientInterceptor,
                              testInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
    }
    companion object {
        val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        const val pakkenavn = "no.nav.familie.ba.soknad.api"
        private const val apiKeyHeader = "x-nav-apiKey"
    }
}

class TestInterceptor: ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val contextHolder = SpringTokenValidationContextHolder()

        log.info("\nFØR")
        log.info(request.headers.toString())
        log.info(body.toString(Charsets.UTF_8))
        log.info(contextHolder.tokenValidationContext.getClaims("selvbetjening").allClaims.toString())
        log.info(contextHolder.tokenValidationContext.getJwtToken("selvbetjening").tokenAsString)
        log.info(contextHolder.tokenValidationContext.issuers.toString())
        log.info("ETTER")
        return execution.execute(request, body)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
    }
}
