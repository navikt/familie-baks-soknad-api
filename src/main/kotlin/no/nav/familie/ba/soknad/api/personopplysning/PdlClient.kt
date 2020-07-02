package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.Pingable
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI


@Component
class PdlClient(@Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
                @Qualifier("restKlientMedApiKey") private val restOperations: RestOperations)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    private val pdlUri: URI = URI.create("$pdlBaseUrl/graphql")

    private fun hentPersonData(personIdent: String, query: String): PdlHentPersonResponse {
        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent), query = query)
        try {
            val response = postForEntity<PdlHentPersonResponse>(uri = pdlUri, payload = pdlPersonRequest)
            if (!response.harFeil()) {
                return response
            } else {
                responsFailure.increment()
                throw Exception(response.errorMessages())
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun hentNavn(personIdent: String): PdlHentPersonResponse {
        val query = this::class.java.getResource("/pdl/hentnavn.graphql").readText().graphqlCompatible()
        return hentPersonData(personIdent, query)
    }

    fun hentNavnOgRelasjoner(personIdent: String): PdlHentPersonResponse {
        val query = this::class.java.getResource("/pdl/hentperson-med-relasjoner.graphql").readText().graphqlCompatible()
        return hentPersonData(personIdent, query)
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
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}
