package no.nav.familie.baks.soknad.api.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.familie.baks.soknad.api.clients.pdl.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.baks.soknad.api.clients.pdl.Adressebeskyttelse
import no.nav.familie.baks.soknad.api.clients.pdl.PdlDoedsafall
import no.nav.familie.baks.soknad.api.clients.pdl.PdlHentPersonResponse
import no.nav.familie.baks.soknad.api.clients.pdl.PdlNavn
import no.nav.familie.baks.soknad.api.clients.pdl.PdlStatsborgerskap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PdlGraphqlTest {
    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonMedFlereRelasjoner.json")), PdlHentPersonResponse::class.java)
        assertEquals(
            "23058518298",
            resp.data.person
                ?.folkeregisteridentifikator
                ?.firstOrNull()
                ?.identifikasjonsnummer
        )
        assertEquals(listOf(PdlDoedsafall(null)), resp.data.person?.doedsfall)
        assertEquals(
            "ENGASJERT",
            resp.data.person!!
                .navn
                .first()
                .fornavn
        )
        assertEquals(
            "FYR",
            resp.data.person!!
                .navn
                .first()
                .etternavn
        )
        assertEquals(
            2,
            resp.data.person!!
                .forelderBarnRelasjon.size
        )
        assertEquals(
            null,
            resp.data.person!!
                .bostedsadresse
                .first()!!
                .matrikkeladresse
        )
        assertEquals(
            null,
            resp.data.person!!
                .bostedsadresse
                .first()!!
                .ukjentBosted
        )
        assertEquals(
            3,
            resp.data.person!!
                .bostedsadresse
                .first()!!
                .vegadresse!!
                .matrikkelId
        )
        assertEquals(
            "E22",
            resp.data.person!!
                .bostedsadresse
                .first()!!
                .vegadresse!!
                .husnummer
        )
        assertEquals(listOf(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.UGRADERT)), resp.data.person!!.adressebeskyttelse)
        assertEquals(listOf(PdlStatsborgerskap("NOR")), resp.data.person!!.statsborgerskap)
        assertEquals(
            "GIFT",
            resp.data.person!!
                .sivilstand
                ?.firstOrNull()
                ?.type
                ?.name
        )
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")), PdlHentPersonResponse::class.java)
        assertTrue(resp.harFeil())
        assertTrue(resp.errorMessages().contains("Fant ikke person"))
        assertTrue(resp.errorMessages().contains("Ikke tilgang"))
        assertTrue(
            resp.errors
                ?.get(0)
                ?.extensions
                ?.code
                .equals("unauthorized")
        )
        assertTrue(
            resp.errors
                ?.get(0)
                ?.extensions
                ?.details
                ?.type
                .equals("abac-deny")
        )
        assertTrue(
            resp.errors
                ?.get(0)
                ?.extensions
                ?.details
                ?.cause
                .equals("cause-0001-manglerrolle")
        )
        assertTrue(
            resp.errors
                ?.get(0)
                ?.extensions
                ?.details
                ?.policy
                .equals("adressebeskyttelse_strengt_fortrolig_adresse")
        )
    }

    @Test
    fun testFulltNavn() {
        assertEquals(
            "For Mellom Etter",
            PdlNavn(fornavn = "For", mellomnavn = "Mellom", etternavn = "Etter").fulltNavn()
        )
        assertEquals(
            "For Etter",
            PdlNavn(fornavn = "For", etternavn = "Etter").fulltNavn()
        )
    }

    private fun getFile(name: String): String = javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
}
