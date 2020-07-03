package no.nav.familie.ba.soknad.api.personopplysning

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class PersonopplysningerServiceTest {

    private lateinit var personopplysningerService: PersonopplysningerService
    private lateinit var client: PdlClient
    val mapper = objectMapper

    @BeforeEach
    fun setUp() {
        client = mockk()
        personopplysningerService = PersonopplysningerService(client)

        every { client.hentNavn(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonNavn.json")), PdlHentPersonResponse::class.java)

    }

    @Test
    fun `hentPersonInfo skal kun returnere familierelasjoner av type BARN`() {
        every { client.hentNavnOgRelasjoner(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonMedFlereRelasjoner.json")), PdlHentPersonResponse::class.java)

        val person = personopplysningerService.hentPersoninfo("1")

        assertEquals(1, person.barn.size)
        assertEquals("ENGASJERT FYR", person.barn.first().navn)
        assertEquals("12345678910", person.barn.first().ident)
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste hvis det er familierelasjoner, men ingen barn`() {
        every { client.hentNavnOgRelasjoner(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonMedRelasjonerIngenBarn.json")), PdlHentPersonResponse::class.java)

        val person = personopplysningerService.hentPersoninfo("1")

        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `henPersonInfo skal returnere tom liste hvis ingen familierelasjoner`() {
        every { client.hentNavnOgRelasjoner(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonUtenRelasjoner.json")), PdlHentPersonResponse::class.java)

        val person = personopplysningerService.hentPersoninfo("1")

        assertTrue(person.barn.isEmpty())
    }


    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}
