package no.nav.familie.baks.soknad.api.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BarnetrygdSøknadServiceTest {
    private val mottakClient: MottakClient = mockk()
    private val personopplysningerService: PersonopplysningerService = mockk()
    private val barnetrygdSøknadService = BarnetrygdSøknadService(mottakClient, personopplysningerService)

    @Test
    fun `mottaOgSendBarnetrygdsøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom et barn har adressebeskyttelse`() {
        // Arrange
        val barnetrygdSøknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        val personMockk = mockk<Person>()
        val barnetrygdSøknadSlot = slot<BarnetrygdSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns true
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns mockk()

        // Act
        barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
    }

    @Test
    fun `mottaOgSendBarnetrygdsøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom søker har adressebeskyttelse`() {
        // Arrange
        val barnetrygdSøknad =
            BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(adressebeskyttelse = true))
        val personMockk = mockk<Person>()
        val barnetrygdSøknadSlot = slot<BarnetrygdSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns mockk()

        // Act
        barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
    }

    @Test
    fun `mottaOgSendBarnetrygdsøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til false dersom verken søker eller barn har adressebeskyttelse`() {
        // Arrange
        val barnetrygdSøknad =
            BarnetrygdSøknadTestData.barnetrygdSøknad()
        val personMockk = mockk<Person>()
        val barnetrygdSøknadSlot = slot<BarnetrygdSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns mockk()

        // Act
        barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isFalse
    }
}
