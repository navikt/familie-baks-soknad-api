package no.nav.familie.baks.soknad.api.clients.mottak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.http.util.UriUtil
import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI
import no.nav.familie.kontrakter.ba.søknad.v8.Søknad as SøknadV8
import no.nav.familie.kontrakter.ks.søknad.v3.KontantstøtteSøknad as KontantstøtteSøknadV3
import no.nav.familie.kontrakter.ks.søknad.v4.KontantstøtteSøknad as KontantstøtteSøknadV4

@Component
class MottakClient(
    @Value("\${FAMILIE_BAKS_MOTTAK_URL}") private val mottakBaseUrl: String,
    @Qualifier("tokenExchange") private val restOperations: RestOperations
) :
    AbstractPingableRestClient(restOperations, "integrasjon") {
    override val pingUri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/soknad")

    override fun ping() {
        try {
            restOperations.exchange<JsonNode>(pingUri, HttpMethod.OPTIONS)
            LOG.debug("Ping mot familie-baks-mottak OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot familie-baks-mottak feilet")
            throw IllegalStateException("Ping mot familie-baks-mottak feilet", e)
        }
    }

    fun sendBarnetrygdSøknad(søknad: SøknadV8): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/soknad/v8")
        return håndterSendingAvSøknad(uri = uri, søknad = søknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV3): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/kontantstotte/soknad/v3")
        return håndterSendingAvSøknad(uri = uri, søknad = kontantstøtteSøknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV4): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/kontantstotte/soknad/v4")
        return håndterSendingAvSøknad(uri = uri, søknad = kontantstøtteSøknad)
    }

    fun håndterSendingAvSøknad(
        uri: URI,
        søknad: Any
    ): Ressurs<Kvittering> {
        try {
            val multipartBuilder = MultipartBuilder().withJson("søknad", søknad)
            val payload: MultiValueMap<String, Any> = multipartBuilder.build()
            val response =
                postForEntity<Ressurs<Kvittering>>(
                    uri = uri,
                    payload = payload,
                    httpHeaders = MultipartBuilder.MULTIPART_HEADERS
                )
            LOG.info("Sende søknad til mottak OK: ${response.data}")
            return response
        } catch (e: Exception) {
            throw IllegalStateException("Sende søknad til familie-baks-mottak feilet", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MottakClient::class.java)
    }
}
