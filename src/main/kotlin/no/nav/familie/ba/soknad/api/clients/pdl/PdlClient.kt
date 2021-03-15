package no.nav.familie.ba.soknad.api.clients.pdl

import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import no.nav.familie.ba.soknad.api.personopplysning.PdlHentPersonResponse
import no.nav.familie.ba.soknad.api.personopplysning.PdlPersonRequest
import no.nav.familie.ba.soknad.api.personopplysning.PdlPersonRequestVariables
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.http.sts.StsRestClient
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Component
class PdlClient(
    @Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
    @Qualifier("restKlientMedApiKey") private val restOperations: RestOperations,
    private val stsRestClient: StsRestClient
) :
    AbstractRestClient(restOperations, "integrasjon"), Pingable {

    private val pdlUri: URI = URI.create("$pdlBaseUrl/graphql")

    fun hentPerson(personIdent: String): PdlHentPersonResponse {
        val query = this::class.java.getResource("/pdl/hent-person-med-relasjoner.graphql").readText().graphqlCompatible()

        val pdlPersonRequest = PdlPersonRequest(
            variables = PdlPersonRequestVariables(personIdent),
            query = query
        )

        val response = postForEntity<PdlHentPersonResponse>(
            uri = pdlUri,
            payload = pdlPersonRequest,
            httpHeaders = httpHeaders()
        )

        if (!response.harFeil()) {
            return response
        } else {
            throw Exception(response.errorMessages())
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", TEMA)
        }
    }

    override fun ping() {
        try {
            restOperations.exchange<JsonNode>(pdlUri, HttpMethod.OPTIONS)
            LOG.debug("Ping mot PDL-API OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot PDL-API feilet")
            throw IllegalStateException("Ping mot PDL-API feilet", e)
        }
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(PdlClient::class.java)
        const val TEMA: String = "BAR"
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}
