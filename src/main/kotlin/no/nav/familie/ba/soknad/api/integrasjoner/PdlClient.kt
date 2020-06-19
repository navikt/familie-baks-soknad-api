package no.nav.familie.ba.soknad.api.integrasjoner

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.Pingable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.lang.IllegalStateException
import java.net.URI

@Component
class PdlClient(@Value("\${PDL_URL}") private val pdlBaseUrl: String,
                @Qualifier("restOperations") restOperations: RestOperations)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    override fun ping() {
        val uri = URI.create("$pdlBaseUrl/internal/isAlive")
        try {
            getForEntity<JsonNode>(uri)
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
