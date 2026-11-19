package no.nav.familie.baks.soknad.api.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData.søknadsfelt
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadTestData
import no.nav.familie.kontrakter.felles.Ressurs
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.Test
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad as KontantstøtteSøknadV6

class SøknadControllerTest {
    private val kontantstøtteSøknadService = mockk<KontantstøtteSøknadService>()
    private val barnetrygdSøknadService = mockk<BarnetrygdSøknadService>()
    private val søknadController = SøknadController(kontantstøtteSøknadService, barnetrygdSøknadService)

    @Test
    fun søknadsmottakBarnetrygd_returnerer_OK_hvis_søknad_validerer() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        val kvittering = Kvittering("OK", LocalDateTime.now())

        every { barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad) } returns Ressurs.success(kvittering)

        val response = søknadController.søknadsmottakBarnetrygd(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
    }

    @Test
    fun søknadsmottakBarnetrygd_returnerer_bad_request_ved_ugyldig_input() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn <>")))
        val response = søknadController.søknadsmottakBarnetrygd(søknad)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
        assertEquals("Søknaden har valideringsfeil i objectPaths: søker.navn.verdi", response.body?.melding)
        verify(exactly = 0) { barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(any<BarnetrygdSøknadV10>()) }
    }

    @Test
    fun søknadsmottakKontantstøtte_returnerer_OK_kvittering_hvis_søknad_validerer() {
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val kvittering = Kvittering("OK", LocalDateTime.now())

        every { kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(søknad) } returns Ressurs.success(kvittering)

        val response = søknadController.søknadsmottakKontantstøtte(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
    }

    @Test
    fun `ugyldig kontantstøttesøknad avvises uten videresending`() {
        val søker = KontantstøtteSøknadTestData.søker().copy(navn = KontantstøtteSøknadTestData.søknadsfelt("Navn", "Navn <>"))
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad(søker = søker)

        val response = søknadController.søknadsmottakKontantstøtte(søknad)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        verify(exactly = 0) { kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(any<KontantstøtteSøknadV6>()) }
    }
}
