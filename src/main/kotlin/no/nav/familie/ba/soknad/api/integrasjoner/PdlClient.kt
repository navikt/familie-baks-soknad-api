package no.nav.familie.ba.soknad.api.integrasjoner

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.Pingable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.lang.IllegalStateException
import java.net.URI

@Component
class PdlClient(@Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
                @Qualifier("restKlientMedApiKey") private val restOperations: RestOperations)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    override fun ping() {
        val uri = URI.create("$pdlBaseUrl/graphql")
        try {
            restOperations.exchange<JsonNode>(uri, HttpMethod.OPTIONS)
            LOG.debug("Ping mot PDL-API OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot PDL-API feilet")
            throw IllegalStateException("Ping mot PDL-API feilet", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlClient::class.java)
    }
}