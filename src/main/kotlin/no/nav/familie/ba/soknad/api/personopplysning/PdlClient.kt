package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.JsonNode
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
import java.net.URI


@Component
class PdlClient(@Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
                @Qualifier("restKlientMedApiKey") private val restOperations: RestOperations,
                private val stsRestClient: StsRestClient)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    private val pdlUri: URI = URI.create("$pdlBaseUrl/graphql")

    private fun hentPersonData(personIdent: String, query: String): PdlHentPersonResponse {
        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent), query = query)
        try {
            log.info("Henter persondata fra pdl")
            secureLogger.info("HttpHeaders: ${httpHeaders()}")
            val response = postForEntity<PdlHentPersonResponse>(uri = pdlUri, payload = pdlPersonRequest, httpHeaders = httpHeaders())
            if (!response.harFeil()) {
                return response
            } else {
                throw Exception(response.errorMessages())
            }
        } catch (e: Exception) {
            log.info("Feilet ved henting av persondata fra pdl: ${e.message}")
            throw e
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", TEMA)
        }
    }

    fun hentNavn(personIdent: String): PdlHentPersonResponse {
        log.info("Henter navn fra pdl")
        val query = this::class.java.getResource("/pdl/hentnavn.graphql").readText().graphqlCompatible()
        return hentPersonData(personIdent, query)
    }

    fun hentNavnOgRelasjoner(personIdent: String): PdlHentPersonResponse {
        log.info("Henter person med relasjoner fra pdl")
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
        private const val TEMA: String = "BAR"
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}
