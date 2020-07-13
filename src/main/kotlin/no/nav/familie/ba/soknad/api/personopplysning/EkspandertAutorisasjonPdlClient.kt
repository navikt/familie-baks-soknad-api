package no.nav.familie.ba.soknad.api.personopplysning


import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.sts.StsRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI


@Component
class EkspandertAutorisasjonPdlClient(@Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
                @Qualifier("ekspandertAutorisasjonRestKlientMedApiKey") private val restOperations: RestOperations,
                                      private val stsRestClient: StsRestClient)
    : AbstractRestClient(restOperations, "integrasjon") {

    private val pdlUri: URI = URI.create("$pdlBaseUrl/graphql")

    fun hentBarn(personIdent: String): PdlHentBarnResponse {
        val query = this::class.java.getResource("/pdl/hent-barn.graphql").readText().graphqlCompatible()
        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent), query = query)
        val response = postForEntity<PdlHentBarnResponse>(uri = pdlUri, payload = pdlPersonRequest, httpHeaders = httpHeaders())
        if (!response.harFeil()) {
            return response
        } else {
            throw Exception(response.errorMessages())
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", PdlClient.TEMA)
        }
    }


}
