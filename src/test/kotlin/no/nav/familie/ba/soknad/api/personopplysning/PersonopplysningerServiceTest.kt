package no.nav.familie.ba.soknad.api.personopplysning

import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.assertFailsWith
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.Matrikkeladresse
import no.nav.familie.kontrakter.felles.personopplysning.UkjentBosted
import no.nav.familie.kontrakter.felles.personopplysning.Vegadresse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonopplysningerServiceTest {

    private lateinit var personopplysningerService: PersonopplysningerService
    private lateinit var pdlClient: PdlClient
    private lateinit var barnePdlClient: BarnePdlClient
    private val mapper = objectMapper
    private val gyldigBostedAdresse = Bostedsadresse(null, Matrikkeladresse(3, "E67", "tillegg", "1456", "1223"), null)

    @BeforeEach
    fun setUp() {
        pdlClient = mockk()
        barnePdlClient = mockk()
        personopplysningerService = PersonopplysningerService(pdlClient, barnePdlClient)

        every { barnePdlClient.hentBarn(any()) } returns
            mapper.readValue(File(getFile("pdl/pdlPersonBarn.json")), PdlHentBarnResponse::class.java)
    }

    @Test
    fun `hentPersonInfo skal kun returnere familierelasjoner av type BARN`() {
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        val person = personopplysningerService.hentPersoninfo("1")

        assertEquals(1, person.barn.size)
        assertEquals("ENGASJERT FYR", person.barn.first().navn)
        assertEquals("12345678910", person.barn.first().ident)
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste hvis det er familierelasjoner, men ingen barn`() {
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonMedRelasjonerIngenBarn")
        val person = personopplysningerService.hentPersoninfo("1")
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `hentPersonInfo skal returnere liste med statsborgerskap hvis det er flere fra pdl`() {
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonMedFlereStatsborgerskap")
        val person = personopplysningerService.hentPersoninfo("1")
        assertEquals(person.statsborgerskap.size, 2)
        assertEquals(person.statsborgerskap[0].landkode, "NOR")
        assertEquals(person.statsborgerskap[1].landkode, "SWE")
    }

    @Test
    fun `henPersonInfo skal returnere tom liste hvis ingen familierelasjoner`() {
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonUtenRelasjoner")
        val person = personopplysningerService.hentPersoninfo("1")
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `borMedSøker skal returnere false når søkerAdressen er null`() {
        val søkerAdresse = null
        val barneAdresse = null
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse, barneAdresse)

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere true når adressene til barn og søker er like`() {
        val borMedSøker = personopplysningerService.borMedSøker(
            søkerAdresse = gyldigBostedAdresse,
            barneAdresse = gyldigBostedAdresse
        )

        assertTrue(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere false når søker og barn har ulik adresse, men lik type`() {
        val barneAdresse = gyldigBostedAdresse.copy(
            matrikkeladresse = Matrikkeladresse(
                1,
                "E2",
                "tillegg",
                "1456",
                "1223"
            )
        )
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse = gyldigBostedAdresse, barneAdresse = barneAdresse)

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere false hvis søkerAdresse er ukjent`() {
        val ukjentAdresse = Bostedsadresse(null, null, UkjentBosted("oslo"))
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse = ukjentAdresse, barneAdresse = ukjentAdresse)

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere true hvis flere adresser finnes, og minst en matcher`() {
        val søkerAdresse = gyldigBostedAdresse.copy(
            vegadresse = Vegadresse(
                adressenavn = "adresse",
                husbokstav = "A",
                bruksenhetsnummer = "1",
                husnummer = "1",
                kommunenummer = "1",
                matrikkelId = 1,
                postnummer = "0101",
                tilleggsnavn = "tillegg"
            )
        )
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse = søkerAdresse, barneAdresse = gyldigBostedAdresse)

        assertTrue(borMedSøker)
    }

    @Test
    fun `hentPerson skal feile dersom barn har gradert adresse`() {
        every { barnePdlClient.hentBarn(any()) } returns
            mapper.readValue(File(getFile("pdl/pdlPersonBarnGradertAdresse.json")), PdlHentBarnResponse::class.java)
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonMedEttBarn")

        assertFailsWith<GradertAdresseException> {
            personopplysningerService.hentPersoninfo("12345678901")
        }
    }

    @Test
    fun `hentPerson skal feile dersom person har gradert adresse`() {
        every { pdlClient.hentSøker(any()) } returns pdlMockFor("pdlPersonUtenRelasjonerGradertAdresse")

        assertFailsWith<GradertAdresseException> {
            personopplysningerService.hentPersoninfo("12345678901")
        }
    }

    private fun pdlMockFor(filNavn: String) = mapper.readValue(
        File(getFile("pdl/$filNavn.json")), PdlHentSøkerResponse::class.java
    )

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}
