package no.nav.familie.baks.soknad.api.config

import no.nav.familie.baks.soknad.api.MockOAuth2ServerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigTest : MockOAuth2ServerConfig() {
    @LocalServerPort
    private var port: Int = 0

    private val restClient =
        RestClient
            .builder()
            .defaultStatusHandler(HttpStatusCode::isError) { _, _ -> }
            .build()

    private fun getMedToken(
        path: String,
        token: String?
    ) = restClient
        .get()
        .uri("http://localhost:$port$path")
        .apply { token?.let { header(HttpHeaders.AUTHORIZATION, "Bearer $it") } }
        .retrieve()
        .toBodilessEntity()

    @Nested
    inner class ÅpneEndepunkter {
        @Test
        fun `internal-endepunkt er tilgjengelig uten autentisering`() {
            val response = getMedToken("/internal/health", token = null)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Nested
    inner class UtenToken {
        @Test
        fun `api-kall uten token returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", token = null)
            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }

    @Nested
    inner class MedGyldigToken {
        @Test
        fun `api-kall med gyldig token og idporten-loa-high returnerer 200`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(claims = mapOf("acr" to SecurityConfig.IDPORTEN_LOA_HIGH)))

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `api-kall med gyldig token og Level4 returnerer 200`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(claims = mapOf("acr" to SecurityConfig.LEVEL4)))

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Nested
    inner class MedUgyldigToken {
        @Test
        fun `api-kall med for lavt acr-nivå returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(claims = mapOf("acr" to "Level3")))

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        fun `api-kall med feil audience returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(audience = "feil-audience"))

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        fun `api-kall med token fra ukjent utsteder returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(issuerId = "feil-issuer"))

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        fun `api-kall med token uten acr-claim returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(claims = emptyMap()))

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        fun `api-kall med utløpt token returnerer 401`() {
            val response = getMedToken("/api/innlogget/barnetrygd", lagTestToken(expiry = -1))

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }
}
