package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PdlGraphqlTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonMedFlereRelasjoner.json")), PdlHentSøkerResponse::class.java)
        assertEquals("ENGASJERT", resp.data.person!!.navn.first().fornavn)
        assertEquals("FYR", resp.data.person!!.navn.first().etternavn)
        assertEquals(2, resp.data.person!!.familierelasjoner.size)
        assertEquals(null, resp.data.person!!.bostedsadresse.first()!!.matrikkeladresse)
        assertEquals(null, resp.data.person!!.bostedsadresse.first()!!.ukjentBosted)
        assertEquals(3, resp.data.person!!.bostedsadresse.first()!!.vegadresse!!.matrikkelId)
        assertEquals("E22", resp.data.person!!.bostedsadresse.first()!!.vegadresse!!.husnummer)
        assertEquals(listOf(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.UGRADERT)), resp.data.person!!.adressebeskyttelse)
        assertEquals(listOf(PdlStatsborgerskap("NOR")), resp.data.person!!.statsborgerskap)
        assertEquals("GIFT", resp.data.person!!.sivilstand.firstOrNull()?.type?.name)
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")), PdlHentSøkerResponse::class.java)
        assertTrue(resp.harFeil())
        assertTrue(resp.errorMessages().contains("Fant ikke person"))
        assertTrue(resp.errorMessages().contains("Ikke tilgang"))
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

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}
