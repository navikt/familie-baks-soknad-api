package no.nav.familie.baks.soknad.api.clients.mottak

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
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
    fun hentKontonummer(kontohaver: String): KontoregisterResponseDto {
        val uri: URI = UriUtil.uri(URI.create(kontoregisterBaseUrl), "hent-aktiv-konto")
        return postForEntity<KontoregisterResponseDto>(
            uri = uri,
            payload = KontoregisterRequestDto(kontohaver)
        )
    }
}

data class KontoregisterRequestDto(
    val kontohaver: String
)

data class KontoregisterResponseDto(
    val kontonummer: String
)
