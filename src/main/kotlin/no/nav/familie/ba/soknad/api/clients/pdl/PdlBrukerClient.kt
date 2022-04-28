package no.nav.familie.ba.soknad.api.clients.pdl

import no.nav.familie.http.sts.StsRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class PdlBrukerClient(
    @Value("\${PDL_URL}") private val pdlBaseUrl: String,
    @Qualifier("restKlientMedApiKeyOgBrukerToken") private val restOperations: RestOperations,
    private val stsRestClient: StsRestClient
) : PdlClient(pdlBaseUrl, restOperations) {

    override fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", TEMA)
        }
    }
}
