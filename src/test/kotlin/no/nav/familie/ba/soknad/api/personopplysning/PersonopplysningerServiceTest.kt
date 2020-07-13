package no.nav.familie.ba.soknad.api.personopplysning

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import no.nav.familie.kontrakter.felles.personinfo.Matrikkeladresse
import no.nav.familie.kontrakter.felles.personinfo.UkjentBosted
import no.nav.familie.kontrakter.felles.personinfo.Vegadresse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class PersonopplysningerServiceTest {

    private lateinit var personopplysningerService: PersonopplysningerService
    private lateinit var client: PdlClient
    private lateinit var authClient: EkspandertAutorisasjonPdlClient
    val mapper = objectMapper
    private val gyldigBostedAdresse = Bostedsadresse(null, Matrikkeladresse(3, "E67", "tillegg", "1456", "1223"), null)

    @BeforeEach
    fun setUp() {
        client = mockk()
        authClient = mockk()
        personopplysningerService = PersonopplysningerService(client, authClient)

        every { authClient.hentBarn(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonBarn.json")), PdlHentBarnResponse::class.java)
    }

    @Test
    fun `hentPersonInfo skal kun returnere familierelasjoner av type BARN`() {
        every { client.hentSøker(any()) } returns pdlMockFor("pdlPersonMedFlereRelasjoner")
        val person = personopplysningerService.hentPersoninfo("1")

        assertEquals(1, person.barn.size)
        assertEquals("ENGASJERT FYR", person.barn.first().navn)
        assertEquals("12345678910", person.barn.first().ident)
    }

    @Test
    fun `hentPersonInfo skal returnere tom liste hvis det er familierelasjoner, men ingen barn`() {
        every { client.hentSøker(any()) } returns pdlMockFor("pdlPersonMedRelasjonerIngenBarn")
        val person = personopplysningerService.hentPersoninfo("1")
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `henPersonInfo skal returnere tom liste hvis ingen familierelasjoner`() {
        every { client.hentSøker(any()) } returns pdlMockFor("pdlPersonUtenRelasjoner")
        val person = personopplysningerService.hentPersoninfo("1")
        assertTrue(person.barn.isEmpty())
    }

    @Test
    fun `borMedSøker skal returnere false når søkerAdressen er null` () {
        val søkerAdresse = null
        val barneAdresse = null
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse, barneAdresse)

        assertFalse(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere true når adressene til barn og søker er like`(){
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse = gyldigBostedAdresse, barneAdresse = gyldigBostedAdresse)

        assertTrue(borMedSøker)
    }

    @Test
    fun `borMedSøker skal returnere false når søker og barn har ulik adresse, men lik type`(){
        val barneAdresse = gyldigBostedAdresse.copy(matrikkeladresse=Matrikkeladresse(1, "E2", "tillegg", "1456", "1223"))
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
        val søkerAdresse = gyldigBostedAdresse.copy(vegadresse = Vegadresse(adressenavn = "adresse", husbokstav = "A", bruksenhetsnummer = "1", husnummer = "1", kommunenummer = "1", matrikkelId = 1, postnummer = "0101", tilleggsnavn = "tillegg"))
        val borMedSøker = personopplysningerService.borMedSøker(søkerAdresse = søkerAdresse, barneAdresse = gyldigBostedAdresse)

        assertTrue(borMedSøker)
    }

    @Test
    fun `hentPerson skal feile dersom barn har gradert adresse`() {
        every { authClient.hentBarn(any()) } returns
                mapper.readValue(File(getFile("pdl/pdlPersonBarnGradertAdresse.json")), PdlHentBarnResponse::class.java)
        every { client.hentSøker(any()) } returns pdlMockFor("pdlPersonMedEttBarn")

        assertFailsWith<GradertAdresseException> {
            personopplysningerService.hentPersoninfo("12345678901")
        }
    }

    @Test
    fun `hentPerson skal feile dersom person har gradert adresse`() {
        every { client.hentSøker(any()) } returns pdlMockFor("pdlPersonUtenRelasjonerGradertAdresse")

        assertFailsWith<GradertAdresseException> {
            personopplysningerService.hentPersoninfo("12345678901")
        }
    }

    private fun pdlMockFor(filNavn: String) = mapper.readValue(File(getFile("pdl/$filNavn.json")), PdlHentSøkerResponse::class.java)


    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}
