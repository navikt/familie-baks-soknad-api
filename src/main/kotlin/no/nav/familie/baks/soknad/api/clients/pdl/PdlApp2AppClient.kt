package no.nav.familie.baks.soknad.api.clients.pdl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PdlApp2AppClient(
    @Value("\${PDL_URL}") pdlBaseUrl: String,
    @Qualifier("pdlClientCredentialRestClient") restClient: RestClient
) : PdlClient(pdlBaseUrl, restClient)
