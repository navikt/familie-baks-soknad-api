package no.nav.familie.baks.soknad.api.clients.kontoregister

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class KontoregisterClient(
    @Value("\${KONTOREGISTER_URL}") private val kontoregisterBaseUrl: String,
    @Qualifier("kontoregisterTokenXRestClient") private val restClient: RestClient
) {
    fun hentKontonummer(kontohaver: String): KontoregisterResponseDto {
        val uri = URI.create("$kontoregisterBaseUrl/hent-aktiv-konto")
        return restClient
            .post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(KontoregisterRequestDto(kontohaver))
            .retrieve()
            .body<KontoregisterResponseDto>()!!
    }
}

data class KontoregisterRequestDto(
    val kontohaver: String
)

data class KontoregisterResponseDto(
    val kontonummer: String
)
