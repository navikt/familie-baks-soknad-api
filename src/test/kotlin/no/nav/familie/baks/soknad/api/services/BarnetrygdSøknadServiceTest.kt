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
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9

class BarnetrygdSøknadServiceTest {
    private val mottakClient: MottakClient = mockk()
    private val personopplysningerService: PersonopplysningerService = mockk()
    private val barnetrygdSøknadService = BarnetrygdSøknadService(mottakClient, personopplysningerService)

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @Test
    fun `v9 erstatter søkeridentitet og beholder kvittering`() {
        val søknad = jsonMapper.convertValue(BarnetrygdSøknadTestData.barnetrygdSøknad(), BarnetrygdSøknadV9::class.java).copy(kontraktVersjon = 9)
        val sendt = slot<BarnetrygdSøknadV9>()
        val person = mockk<Person>()
        val kvittering = Ressurs.success(Kvittering("OK", LocalDateTime.of(2026, 6, 15, 12, 0)))
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns "legacy-token-subject"
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.BARNETRYGD, true) } returns person
        every { person.adressebeskyttelse } returns false
        every { mottakClient.sendBarnetrygdSøknad(capture(sendt)) } returns kvittering

        val resultat = barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad)

        assertThat(sendt.captured.søker.ident.verdi.values).isNotEmpty.containsOnly("legacy-token-subject")
        assertThat(sendt.captured.barn).isEqualTo(søknad.barn)
        assertThat(sendt.captured.finnesPersonMedAdressebeskyttelse).isFalse
        assertThat(resultat).isSameAs(kvittering)
    }

    @Test
    fun `mottaOgSendBarnetrygdsøknad - skal hente ident fra token og sette finnesPersonMedAdressebeskyttelse til true dersom et barn har adressebeskyttelse`() {
        // Arrange
        val barnetrygdSøknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        val personMockk = mockk<Person>()
        val barnetrygdSøknadSlot = slot<BarnetrygdSøknad>()
        val fnrFraToken = "12345678910"
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns fnrFraToken
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.BARNETRYGD, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns true
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns mockk()

        // Act
        barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
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
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.BARNETRYGD, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns mockk()

        // Act
        barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
        assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isTrue
        verify(exactly = 0) { personopplysningerService.hentPersoninfo(any(), any(), any()) }
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
        every { personopplysningerService.hentPersoninfo(any(), Ytelse.BARNETRYGD, true) } returns personMockk
        every { personMockk.adressebeskyttelse } returns false
        val kvittering = Ressurs.success(Kvittering("OK", LocalDateTime.of(2026, 6, 15, 12, 0)))
        every { mottakClient.sendBarnetrygdSøknad(capture(barnetrygdSøknadSlot)) } returns kvittering

        // Act
        val resultat = barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(barnetrygdSøknad)

        // Assert
        val videresendtSøknad = barnetrygdSøknadSlot.captured
        assertThat(videresendtSøknad.søker.ident.verdi.values).isNotEmpty.containsOnly(fnrFraToken)
        assertThat(videresendtSøknad.finnesPersonMedAdressebeskyttelse).isFalse
        assertThat(resultat).isSameAs(kvittering)
        barnetrygdSøknad.barn.forEach {
            verify(exactly = 1) {
                personopplysningerService.hentPersoninfo(
                    it.ident.verdi.values
                        .first(),
                    Ytelse.BARNETRYGD,
                    true
                )
            }
        }
    }
}
