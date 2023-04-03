package no.nav.familie.baks.soknad.api.clients.pdl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class PdlBrukerClient(
    @Value("\${PDL_URL}") private val pdlBaseUrl: String,
    @Qualifier("tokenExchange") private val restOperations: RestOperations
) : PdlClient(pdlBaseUrl, restOperations)
