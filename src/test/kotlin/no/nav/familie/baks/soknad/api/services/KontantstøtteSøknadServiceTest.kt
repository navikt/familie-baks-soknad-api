package no.nav.familie.baks.soknad.api.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class KontantstøtteSøknadServiceTest {
    private val mottakClient: MottakClient = mockk()
    private val personopplysningerService: PersonopplysningerService = mockk()
    private val kontantstøtteSøknadService = KontantstøtteSøknadService(mottakClient, personopplysningerService)

    @Test
    fun `mottaOgSendKontantstøttesøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom et barn har adressebeskyttelse`() {
        // Arrange
        val kontantstøtteSøknad = KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val personMockk = mockk<Person>()
        val kontantstøtteSøknadSlot = slot<KontantstøtteSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns true
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns mockk()

        // Act
        kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        Assertions.assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
    }

    @Test
    fun `mottaOgSendKontantstøttesøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom søker har adressebeskyttelse`() {
        // Arrange
        val kontantstøtteSøknad =
            KontantstøtteSøknadTestData.kontantstøtteSøknad(
                søker =
                    KontantstøtteSøknadTestData
                        .søker()
                        .copy(adressebeskyttelse = true)
            )
        val personMockk = mockk<Person>()
        val kontantstøtteSøknadSlot = slot<KontantstøtteSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns mockk()

        // Act
        kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        Assertions.assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
    }

    @Test
    fun `mottaOgSendKontantstøttesøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til false dersom verken søker eller barn har adressebeskyttelse`() {
        // Arrange
        val kontantstøtteSøknad =
            KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val personMockk = mockk<Person>()
        val kontantstøtteSøknadSlot = slot<KontantstøtteSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns mockk()

        // Act
        kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(
            videresendtSøknad.søker.ident.verdi.values
                .all { it == fnrFraToken }
        )
        Assertions.assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isFalse
    }
}
