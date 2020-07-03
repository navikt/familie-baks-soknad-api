package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ba.soknad.api.integrasjoner.ClientMocks
import no.nav.familie.kontrakter.felles.objectMapper
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
    }

    @Test
    fun `hentPersonInfo skal kun returnere familierelasjoner av type BARN`() {
        every { client.hentNavnOgRelasjoner(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonMedEttBarn.json")), PdlHentPersonResponse::class.java)
        every { client.hentNavn(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonNavn.json")), PdlHentPersonResponse::class.java)

        val person = personopplysningerService.hentPersoninfo("1")
    }


    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}
