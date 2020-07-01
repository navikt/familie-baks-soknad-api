package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.Pingable
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.lang.IllegalStateException
import java.net.URI


@Component
class PdlClient(@Value("\${PDL_API_URL}") private val pdlBaseUrl: String,
                @Qualifier("restKlientMedApiKey") private val restOperations: RestOperations)
    : AbstractRestClient(restOperations, "integrasjon"), Pingable {

    val pdlUri: URI = URI.create("$pdlBaseUrl/graphql")
    private val personInfoQuery = this::class.java.getResource("/pdl/hentperson-med-relasjoner.graphql").readText().graphqlCompatible()

    private fun hentPersonData(personIdent: String): PdlHentPersonResponse {
        val pdlPersonRequest = PdlPersonRequest(variables = personIdent, query = personInfoQuery)
        try {
            val response = postForEntity<PdlHentPersonResponse>(uri = pdlUri, payload = pdlPersonRequest)
            if (!response.harFeil()) {
                return response
            } else {
                responsFailure.increment()
                throw Exception("feil:S")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun hentNavn(personIdent: String): String {
        val response = hentPersonData(personIdent)
        return Result.runCatching {
            response.data.person!!.navn.first().fulltNavn()
        }.fold(
                onSuccess = { it },
                onFailure = { throw it}
        )
    }

    fun hentPerson(personIdent: String): Person {
        val response = hentPersonData(personIdent)
        return Result.runCatching {
            val barn: Set<Barn> = response.data.person!!.familierelasjoner.filter {
                relasjon -> relasjon.relatertPersonsRolle == FAMILIERELASJONSROLLE.BARN
            }.map {
                relasjon -> Barn(Personident(relasjon.relatertPersonsIdent), hentNavn(relasjon.relatertPersonsIdent))
            }.toSet()

            response.data.person.let {
                Person(navn = it.navn.first().fulltNavn(), barn = barn)
            }
        }.fold(
                onSuccess = { it },
                onFailure = { throw it}
        )
    }

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
        private val LOG = LoggerFactory.getLogger(PdlClient::class.java)
    }
}

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}