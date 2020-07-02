package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class PdlGraphqlTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonUtenRelasjoner.json")), PdlHentPersonResponse::class.java)


        assertEquals("ENGASJERT", resp.data.person!!.navn.first().fornavn)
        assertEquals("FYR", resp.data.person!!.navn.first().fornavn)
    }

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}