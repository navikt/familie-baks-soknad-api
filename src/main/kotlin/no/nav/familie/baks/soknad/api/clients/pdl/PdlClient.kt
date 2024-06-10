package no.nav.familie.baks.soknad.api.clients.pdl

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.http.util.UriUtil
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI

abstract class PdlClient(
    pdlBaseUrl: String,
    private val restOperations: RestOperations
) : AbstractPingableRestClient(restOperations, "pdl.integrasjon"),
    Pingable {
    private val pdlUri = UriUtil.uri(base = URI.create(pdlBaseUrl), path = "graphql")

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
            postForEntity<PdlHentPersonResponse>(
                uri = pdlUri,
                payload = pdlPersonRequest,
                httpHeaders = httpHeaders(ytelse)
            )

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

    private fun httpHeaders(ytelse: Ytelse): HttpHeaders =
        HttpHeaders().apply {
            add("Tema", ytelse.tema.name)
            add("behandlingsnummer", ytelse.tema.behandlingsnummer)
        }

    override val pingUri: URI
        get() = pdlUri

    override fun ping() {
        try {
            restOperations.exchange<JsonNode>(pdlUri, HttpMethod.OPTIONS)
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
