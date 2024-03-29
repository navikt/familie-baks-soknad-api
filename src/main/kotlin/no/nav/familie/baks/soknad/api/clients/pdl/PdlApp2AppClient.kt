package no.nav.familie.baks.soknad.api.clients.pdl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class PdlApp2AppClient(
    @Value("\${PDL_URL}") private val pdlBaseUrl: String,
    @Qualifier("clientCredential") private val restOperations: RestOperations
) : PdlClient(pdlBaseUrl, restOperations)
