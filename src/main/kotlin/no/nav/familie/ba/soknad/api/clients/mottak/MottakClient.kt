package no.nav.familie.ba.soknad.api.clients.mottak

import com.fasterxml.jackson.databind.JsonNode
import java.lang.IllegalStateException
import java.net.URI
import no.nav.familie.ba.soknad.api.domene.Kvittering
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.kontrakter.ba.søknad.v5.Søknad
import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Component
class MottakClient(
    @Value("\${FAMILIE_BA_MOTTAK_URL}") private val mottakBaseUrl: String,
    @Qualifier("restKlientMottak") private val restOperations: RestOperations
) :
    AbstractPingableRestClient(restOperations, "integrasjon") {

    override val pingUri: URI = URI.create("$mottakBaseUrl/api/soknad")

    override fun ping() {
        try {
            restOperations.exchange<JsonNode>(pingUri, HttpMethod.OPTIONS)
            LOG.debug("Ping mot familie-ba-mottak OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot familie-ba-mottak feilet")
            throw IllegalStateException("Ping mot familie-ba-mottak feilet", e)
        }
    }

    fun sendSøknad(søknad: Søknad): Ressurs<Kvittering> {
        val uri: URI = URI.create("$mottakBaseUrl/api/soknad/v5")

        try {
            val multipartBuilder = MultipartBuilder().withJson("søknad", søknad)

            val response = postForEntity<Ressurs<Kvittering>>(
                uri = uri,
                payload = multipartBuilder.build(),
                httpHeaders = MultipartBuilder.MULTIPART_HEADERS
            )
            LOG.info("Sende søknad til mottak OK: ${response.data}")
            return response
        } catch (e: Exception) {
            throw IllegalStateException("Sende søknad til familie-ba-mottak feilet", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MottakClient::class.java)
    }
}
