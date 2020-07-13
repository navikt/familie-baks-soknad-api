package no.nav.familie.ba.soknad.api.config

import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.net.URI

@Component
class IntegrasjonsConfig {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Bean
    fun stsRestClient(
            @Value("\${STS_URL}") stsUrl: URI,
            @Value("\${CREDENTIAL_USERNAME}") stsUsername: String,
            @Value("\${CREDENTIAL_PASSWORD}") stsPassword: String,
            @Value("\${STS_APIKEY}") stsApiKey: String): StsRestClient {

        val stsFullUrl = URI.create("$stsUrl?grant_type=client_credentials&scope=openid")
        secureLogger.info("Sts apikey: $stsApiKey")
        return StsRestClient(objectMapper, stsFullUrl, stsUsername, stsPassword, stsApiKey)
    }

}