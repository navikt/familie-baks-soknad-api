package no.nav.familie.ba.soknad.api.integrasjoner

import com.fasterxml.jackson.databind.JsonNode
import main.kotlin.no.nav.familie.ba.søknad.Søknad
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.MultipartBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.lang.IllegalStateException
import java.net.URI

@Component
class MottakClient(@Value("\${FAMILIE_BA_MOTTAK_URL}") private val mottakBaseUrl: String,
                   @Qualifier("restKlientMottak") private val restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "integrasjon") {

    override val pingUri: URI = URI.create("$mottakBaseUrl/internal/health")

    override fun ping() {
        try {
            getForEntity<JsonNode>(pingUri)
            LOG.debug("Ping mot familie-ba-mottak OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot familie-ba-mottak feilet")
            throw IllegalStateException("Ping mot familie-ba-mottak feilet", e)
        }
    }

    fun sendSøknad(søknad: Søknad): String {
        val uri: URI = URI.create("$mottakBaseUrl/api/soknad")
        try {
            val multipartBuilder = MultipartBuilder().withJson("søknad", søknad)

            val response = postForEntity<String>(uri = uri, payload = multipartBuilder.build(), httpHeaders = MultipartBuilder.MULTIPART_HEADERS)
            LOG.debug("Sende søknad til mottak OK")
            return response
        } catch (e: Exception) {
            throw IllegalStateException("Sende søknad til familie-ba-mottak feilet", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MottakClient::class.java)
    }
}