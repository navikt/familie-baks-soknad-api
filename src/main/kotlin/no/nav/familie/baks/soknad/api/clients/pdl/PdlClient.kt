package no.nav.familie.baks.soknad.api.clients.pdl

import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.http.util.UriUtil
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

abstract class PdlClient(
    pdlBaseUrl: String,
    private val restOperations: RestOperations
) :
    AbstractPingableRestClient(restOperations, "pdl.integrasjon"), Pingable {

    private val pdlUri = UriUtil.uri(base = URI.create(pdlBaseUrl), path = "graphql")

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

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add("Tema", TEMA)
        }
    }

    override val pingUri: URI
        get() = pdlUri

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

        val LOG: Logger = LoggerFactory.getLogger(PdlApp2AppClient::class.java)
        const val TEMA: String = "BAR"
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}
