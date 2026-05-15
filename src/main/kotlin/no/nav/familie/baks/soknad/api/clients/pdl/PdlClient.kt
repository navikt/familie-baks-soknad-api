package no.nav.familie.baks.soknad.api.clients.pdl

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.baks.soknad.api.domene.Ytelse
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

abstract class PdlClient(
    pdlBaseUrl: String,
    private val restClient: RestClient
) {
    private val pdlUri = URI.create("$pdlBaseUrl/graphql")
    private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    fun hentPerson(
        personIdent: String,
        ytelse: Ytelse
    ): PdlHentPersonResponse {
        val query =
            this::class.java
                .getResource("/pdl/hent-person-med-relasjoner.graphql")
                .readText()
                .graphqlCompatible()

        val pdlPersonRequest =
            PdlPersonRequest(
                variables = PdlPersonRequestVariables(personIdent),
                query = query
            )

        val response =
            restClient
                .post()
                .uri(pdlUri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers { headers ->
                    headers.add("Tema", ytelse.tema.name)
                    headers.add("behandlingsnummer", ytelse.tema.behandlingsnummer)
                }.body(pdlPersonRequest)
                .retrieve()
                .body<PdlHentPersonResponse>()!!

        if (response.harFeil()) {
            LOG.error("Feil ved henting av person fra PDL. Se securelogs for detaljer.")
            secureLogger.error(
                "Feil ved henting av person fra PDL: ${response.errorMessages()}. " +
                    "Behandlingsnummer: ${ytelse.tema.behandlingsnummer}."
            )
            throw Exception("En feil har oppstått ved henting av person")
        }

        if (response.harAdvarsel()) {
            LOG.warn("Advarsel ved henting av person fra PDL. Se securelogs for detaljer.")
            secureLogger.warn(
                "Advarsel ved henting av person fra PDL: ${response.extensions?.warnings}. " +
                    "Behandlingsnummer: ${ytelse.tema.behandlingsnummer}."
            )
        }

        return response
    }

    fun ping() {
        try {
            restClient
                .options()
                .uri(pdlUri)
                .retrieve()
                .body<JsonNode>()
            LOG.debug("Ping mot PDL-API OK")
        } catch (e: Exception) {
            LOG.warn("Ping mot PDL-API feilet")
            throw IllegalStateException("Ping mot PDL-API feilet", e)
        }
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(PdlApp2AppClient::class.java)
    }
}

fun String.graphqlCompatible(): String = StringUtils.normalizeSpace(this.replace("\n", ""))
