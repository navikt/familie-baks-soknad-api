package no.nav.familie.baks.soknad.api.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KontantstøtteSøknadServiceTest {
    private val mottakClient: MottakClient = mockk()
    private val personopplysningerService: PersonopplysningerService = mockk()
    private val kontantstøtteSøknadService = KontantstøtteSøknadService(mottakClient, personopplysningerService)

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @Test
    fun `mottaOgSendKontantstøttesøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom et barn har adressebeskyttelse`() {
        // Arrange
        val kontantstøtteSøknad = KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val personMockk = mockk<Person>()
        val kontantstøtteSøknadSlot = slot<KontantstøtteSøknad>()
        val fnrFraToken = "token-subject"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.KONTANTSTOTTE, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns true
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns mockk()

        // Act
        kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
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
        val fnrFraToken = "token-subject"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.KONTANTSTOTTE, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns mockk()

        // Act
        kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
        Assertions.assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
        verify(exactly = 0) { personopplysningerService.hentPersoninfo(any(), any(), any()) }
    }

    @Test
    fun `mottaOgSendKontantstøttesøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til false dersom verken søker eller barn har adressebeskyttelse`() {
        // Arrange
        val kontantstøtteSøknad =
            KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val personMockk = mockk<Person>()
        val kontantstøtteSøknadSlot = slot<KontantstøtteSøknad>()
        val fnrFraToken = "token-subject"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.KONTANTSTOTTE, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        val kvittering = Ressurs.success(Kvittering("OK", LocalDateTime.of(2026, 6, 15, 12, 0)))
        every { mottakClient.sendKontantstøtteSøknad(capture(kontantstøtteSøknadSlot)) } returns kvittering

        // Act
        val resultat = kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad)

        // Assert
        val videresendtSøknad = kontantstøtteSøknadSlot.captured
        Assertions.assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
        Assertions.assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isFalse
        Assertions.assertThat(resultat).isSameAs(kvittering)
        kontantstøtteSøknad.barn.forEach {
            verify(exactly = 1) {
                personopplysningerService.hentPersoninfo(
                    it.ident.verdi.values
                        .first(),
                    Ytelse.KONTANTSTOTTE,
                    true
                )
            }
        }
    }
}
