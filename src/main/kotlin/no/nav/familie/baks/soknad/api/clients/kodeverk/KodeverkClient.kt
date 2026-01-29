package no.nav.familie.baks.soknad.api.clients.kodeverk

import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.restklient.client.AbstractPingableRestClient
import no.nav.familie.restklient.client.Pingable
import no.nav.familie.restklient.util.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KodeverkClient(
    @Value("\${KODEVERK_URL}") private val kodeverkBaseUrl: String,
    @Qualifier("clientCredential") private val restOperations: RestOperations
) : AbstractPingableRestClient(restOperations, "integrasjon"),
    Pingable {
    private val kodeverkUri: URI = URI.create(kodeverkBaseUrl)

    fun hentAlleLand(): KodeverkDto = getForEntity(uri = kodeverkUri("Landkoder"))

    fun hentEØSLand(): KodeverkDto = getForEntity(uri = kodeverkUri("EEAFreg"))

    fun hentPostnummer(): KodeverkDto = getForEntity(uri = kodeverkUri("Postnummer"))

    fun kodeverkUri(
        kodeverksnavn: String,
        medHistorikk: Boolean = false
    ): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return UriUtil.uri(
            base = kodeverkUri,
            path = "api/v1/kodeverk/$kodeverksnavn/koder/betydninger",
            query = query
        )
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }

    override val pingUri: URI = UriUtil.uri(kodeverkUri, PATH_PING)
}
