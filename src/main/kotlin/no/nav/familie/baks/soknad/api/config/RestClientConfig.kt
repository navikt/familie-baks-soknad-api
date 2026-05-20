package no.nav.familie.baks.soknad.api.config

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.felles.tokenklient.tokenx.TokenXInterceptor
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
    private val entraIDRestClientFactory: EntraIDRestClientFactory,
    private val consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    private val mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor
) {
    @Bean("pdlClientCredentialRestClient")
    fun pdlClientCredentialRestClient(
        @Value("\${PDL_SCOPE}") scope: String
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    @Bean("kodeverkRestClient")
    fun kodeverkRestClient(
        @Value("\${KODEVERK_SCOPE}") scope: String
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    @Bean("mottakTokenXRestClient")
    fun mottakTokenXRestClient(
        tokenXClient: TokenXClient,
        @Value("\${MOTTAK_SCOPE}") scope: String
    ): RestClient = lagTokenXRestClient(tokenXClient, scope)

    @Bean("kontoregisterTokenXRestClient")
    fun kontoregisterTokenXRestClient(
        tokenXClient: TokenXClient,
        @Value("\${KONTOREGISTER_SCOPE}") scope: String
    ): RestClient = lagTokenXRestClient(tokenXClient, scope)

    @Bean("pdlTokenXRestClient")
    fun pdlTokenXRestClient(
        tokenXClient: TokenXClient,
        @Value("\${PDL_TOKENX_SCOPE}") scope: String
    ): RestClient = lagTokenXRestClient(tokenXClient, scope)

    private fun lagTokenXRestClient(
        tokenXClient: TokenXClient,
        scope: String
    ): RestClient =
        RestClient
            .builder()
            .requestInterceptor(TokenXInterceptor(tokenXClient, scope) { EksternBrukerUtils.getBearerTokenForLoggedInUser() })
            .requestInterceptor(consumerIdClientInterceptor)
            .requestInterceptor(mdcValuesPropagatingClientInterceptor)
            .build()
}
