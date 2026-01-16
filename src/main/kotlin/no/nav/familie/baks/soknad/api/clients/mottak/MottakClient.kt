package no.nav.familie.baks.soknad.api.clients.mottak

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.restklient.client.AbstractPingableRestClient
import no.nav.familie.restklient.client.MultipartBuilder
import no.nav.familie.restklient.util.UriUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import tools.jackson.databind.JsonNode
import java.net.URI
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad as KontantstøtteSøknadV6

@Component
class MottakClient(
    @Value("\${FAMILIE_BAKS_MOTTAK_URL}") private val mottakBaseUrl: String,
    @Qualifier("tokenExchange") private val restOperations: RestOperations
) : AbstractPingableRestClient(restOperations, "integrasjon") {
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

    fun sendBarnetrygdSøknad(søknad: BarnetrygdSøknadV10): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/soknad/v10")
        return håndterSendingAvSøknad(uri = uri, søknad = søknad)
    }

    fun sendBarnetrygdSøknad(søknad: BarnetrygdSøknadV9): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/soknad/v9")
        return håndterSendingAvSøknad(uri = uri, søknad = søknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV6): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/kontantstotte/soknad/v6")
        return håndterSendingAvSøknad(uri = uri, søknad = kontantstøtteSøknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV5): Ressurs<Kvittering> {
        val uri: URI = UriUtil.uri(URI.create(mottakBaseUrl), "api/kontantstotte/soknad/v5")
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
