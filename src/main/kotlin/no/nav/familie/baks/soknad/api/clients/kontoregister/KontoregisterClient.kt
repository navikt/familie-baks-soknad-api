package no.nav.familie.baks.soknad.api.clients.mottak

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KontoregisterClient(
    @Value("\${KONTOREGISTER_URL}") private val kontoregisterBaseUrl: String,
    @Qualifier("tokenExchange") private val restOperations: RestOperations
) : AbstractRestClient(restOperations, "kontoregister") {
    fun hentKontonummer(kontohaver: String): Ressurs<KontoregisterResponseDto> {
        val uri: URI = UriUtil.uri(URI.create(kontoregisterBaseUrl), "hent-aktiv-konto")
        return postForEntity<Ressurs<KontoregisterResponseDto>>(
            uri = uri,
            payload = KontoregisterRequestDto(kontohaver)
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MottakClient::class.java)
    }
}

data class KontoregisterRequestDto(
    val kontohaver: String
)

data class KontoregisterResponseDto(
    val kontonummer: String
)
