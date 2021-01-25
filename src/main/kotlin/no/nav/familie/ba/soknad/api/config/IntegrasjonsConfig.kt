package no.nav.familie.ba.soknad.api.config

import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.net.URI

@Component
class IntegrasjonsConfig {

    @Bean
    fun stsRestClient(
            @Value("\${STS_URL}") stsUrl: URI,
            @Value("\${CREDENTIAL_USERNAME}") stsUsername: String,
            @Value("\${CREDENTIAL_PASSWORD}") stsPassword: String,
            @Value("\${STS_APIKEY}") stsApiKey: String): StsRestClient {

        val stsFullUrl = URI.create("$stsUrl?grant_type=client_credentials&scope=openid")
        return StsRestClient(objectMapper, stsFullUrl, stsUsername, stsPassword, stsApiKey)
    }

}
