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
class MottakClient(@Value("\${FAMILIE_BA_MOTTAK_URL}") private val mottakBaseUrl: String,
                   @Qualifier("restOperations") restOperations: RestOperations)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    override fun ping() {
        val uri = URI.create("$mottakBaseUrl/internal/health")
        try {
            getForEntity<JsonNode>(uri)
            LOG.debug("Ping mot familie-ba-mottak OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot familie-ba-mottak feilet")
            throw IllegalStateException("Ping mot familie-ba-mottak feilet", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MottakClient::class.java)
    }
}