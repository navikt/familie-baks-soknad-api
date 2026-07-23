package no.nav.familie.baks.soknad.api.clients.kodeverk

import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class KodeverkClient(
    @Value("\${KODEVERK_URL}") private val kodeverkBaseUrl: String,
    @Qualifier("kodeverkRestClient") private val restClient: RestClient
) {
    private val kodeverkUri: URI = URI.create(kodeverkBaseUrl)

    fun hentAlleLand(): KodeverkDto = getForEntity(uri = kodeverkUri("Landkoder"))

    fun hentEØSLand(): KodeverkDto = getForEntity(uri = kodeverkUri("EEAFreg"))

    fun hentPostnummer(): KodeverkDto = getForEntity(uri = kodeverkUri("Postnummer"))

    fun ping() {
        restClient
            .get()
            .uri(URI.create("$kodeverkBaseUrl/$PATH_PING"))
            .retrieve()
            .body<String>()
    }

    private fun getForEntity(uri: URI): KodeverkDto =
        restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<KodeverkDto>()!!

    fun kodeverkUri(
        kodeverksnavn: String,
        medHistorikk: Boolean = false
    ): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return URI.create("$kodeverkUri/api/v1/kodeverk/$kodeverksnavn/koder/betydninger?$query")
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }
}
