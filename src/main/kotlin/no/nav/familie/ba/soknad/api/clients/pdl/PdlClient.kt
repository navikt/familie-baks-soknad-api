package no.nav.familie.ba.soknad.api.clients.pdl

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.Pingable
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI

abstract class PdlClient(
    pdlBaseUrl: String,
    @Qualifier("tokenExchange") private val restOperations: RestOperations
) :
    AbstractPingableRestClient(restOperations, "pdl.integrasjon"), Pingable {

    private val pdlUri = URI.create("$pdlBaseUrl/graphql")

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
            LOG.info("Code: " + response.errors?.get(0)?.extensions?.code)
            LOG.info("Cause: " + response.errors?.get(0)?.extensions?.details?.cause)
            LOG.info("Policy: " + response.errors?.get(0)?.extensions?.details?.policy)
            LOG.info("Type: " + response.errors?.get(0)?.extensions?.details?.type)
            throw Exception(response.errorMessages())
        }
    }

    abstract fun httpHeaders(): HttpHeaders

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

        val LOG: Logger = LoggerFactory.getLogger(PdlSystemClient::class.java)
        const val TEMA: String = "BAR"
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}
