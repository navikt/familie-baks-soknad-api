package no.nav.familie.ba.soknad.api.clients.pdl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class PdlApp2AppClient(
    @Value("\${PDL_URL}") private val pdlBaseUrl: String,
    @Qualifier("clientCredential") private val restOperations: RestOperations,
) : PdlClient(pdlBaseUrl, restOperations) {

    override fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Tema", TEMA)
        }
    }
}
