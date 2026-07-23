package no.nav.familie.baks.soknad.api.clients.mottak

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import tools.jackson.databind.JsonNode
import java.net.URI
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad as KontantstøtteSøknadV6

@Component
class MottakClient(
    @Value("\${FAMILIE_BAKS_MOTTAK_URL}") private val mottakBaseUrl: String,
    @Qualifier("mottakTokenXRestClient") private val restClient: RestClient
) {
    fun ping() {
        val pingUri = URI.create("$mottakBaseUrl/api/soknad")
        try {
            restClient
                .options()
                .uri(pingUri)
                .retrieve()
                .body<JsonNode>()
            LOG.debug("Ping mot familie-baks-mottak OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot familie-baks-mottak feilet")
            throw IllegalStateException("Ping mot familie-baks-mottak feilet", e)
        }
    }

    fun sendBarnetrygdSøknad(søknad: BarnetrygdSøknadV10): Ressurs<Kvittering> {
        val uri = URI.create("$mottakBaseUrl/api/soknad/v10")
        return håndterSendingAvSøknad(uri = uri, søknad = søknad)
    }

    fun sendBarnetrygdSøknad(søknad: BarnetrygdSøknadV9): Ressurs<Kvittering> {
        val uri = URI.create("$mottakBaseUrl/api/soknad/v9")
        return håndterSendingAvSøknad(uri = uri, søknad = søknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV6): Ressurs<Kvittering> {
        val uri = URI.create("$mottakBaseUrl/api/kontantstotte/soknad/v6")
        return håndterSendingAvSøknad(uri = uri, søknad = kontantstøtteSøknad)
    }

    fun sendKontantstøtteSøknad(kontantstøtteSøknad: KontantstøtteSøknadV5): Ressurs<Kvittering> {
        val uri = URI.create("$mottakBaseUrl/api/kontantstotte/soknad/v5")
        return håndterSendingAvSøknad(uri = uri, søknad = kontantstøtteSøknad)
    }

    fun håndterSendingAvSøknad(
        uri: URI,
        søknad: Any
    ): Ressurs<Kvittering> {
        try {
            val jsonBytes = jsonMapper.writeValueAsBytes(søknad)
            val jsonHeaders = HttpHeaders()
            jsonHeaders.contentType = MediaType.APPLICATION_JSON
            val jsonPart = HttpEntity<ByteArray>(jsonBytes, jsonHeaders)

            val multipartBody = LinkedMultiValueMap<String, Any>()
            multipartBody.add("søknad", jsonPart)

            val response =
                restClient
                    .post()
                    .uri(uri)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipartBody)
                    .retrieve()
                    .body<Ressurs<Kvittering>>()!!
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
