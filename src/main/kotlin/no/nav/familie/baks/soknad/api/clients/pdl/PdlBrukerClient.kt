package no.nav.familie.baks.soknad.api.clients.pdl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PdlBrukerClient(
    @Value("\${PDL_URL}") pdlBaseUrl: String,
    @Qualifier("pdlTokenXRestClient") restClient: RestClient
) : PdlClient(pdlBaseUrl, restClient)
