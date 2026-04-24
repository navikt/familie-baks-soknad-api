package no.nav.familie.baks.soknad.api.controllers

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData.søknadsfelt
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadTestData
import no.nav.familie.kontrakter.felles.Ressurs
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime
import kotlin.test.Test

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
    fun søknadsmottakBarnetrygd_logger_men_kaster_ikke_feil_ved_ugyldig_input() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn <>")))
        val kvittering = Kvittering("OK", LocalDateTime.now())
        every { barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad) } returns Ressurs.success(kvittering)

        val response = søknadController.søknadsmottakBarnetrygd(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
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
}
