package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadTestData.søknadsfelt
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadTestData
import no.nav.familie.kontrakter.felles.Ressurs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import kotlin.test.Test

class SøknadControllerTest {
    private val kontantstøtteSøknadService = mock(KontantstøtteSøknadService::class.java)
    private val barnetrygdSøknadService = mock(BarnetrygdSøknadService::class.java)
    private val søknadController = SøknadController(kontantstøtteSøknadService, barnetrygdSøknadService)

    @Test
    fun søknadsmottakBarnetrygd_returnerer_OK_hvis_søknad_validerer() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad()
        val kvittering = Kvittering("OK", LocalDateTime.now())
        `when`(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad)).thenReturn(Ressurs.success(kvittering))

        val response = søknadController.søknadsmottakBarnetrygd(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
    }

    @Test
    fun søknadsmottakBarnetrygd_logger_men_kaster_ikke_feil_ved_ugyldig_input() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn <>")))
        søknadController.søknadsmottakBarnetrygd(søknad)
        val kvittering = Kvittering("OK", LocalDateTime.now())
        `when`(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad)).thenReturn(Ressurs.success(kvittering))

        val response = søknadController.søknadsmottakBarnetrygd(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
    }

    @Test
    fun søknadsmottakBarnetrygd_kaster_feil_ved_ugyldig_input() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn <>")))
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt inneholder ugyldige tegn"))
    }

    @Test
    fun søknadsmottakKontantstøtte_returnerer_OK_kvittering_hvis_søknad_validerer() {
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad()
        val kvittering = Kvittering("OK", LocalDateTime.now())
        `when`(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(søknad)).thenReturn(Ressurs.success(kvittering))

        val response = søknadController.søknadsmottakKontantstøtte(søknad)

        assertEquals(200, response.statusCode.value())
        assertEquals(kvittering, response.body?.data)
    }

    @Test
    fun søknadsmottakKontantstøtte_kaster_feil_ved_ugyldig_input() {
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad(søker = KontantstøtteSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn <>")))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt inneholder ugyldige tegn"))
    }

    @Test
    fun søknadsmottakKontantstøtte_kaster_feil_ved_for_lang_input() {
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad(søker = KontantstøtteSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn er over 200 tegn".padEnd(200, 'a'))))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt er for langt"))
    }

    @Test
    fun søknadsmottakBarnetrygd_kaster_feil_ved_for_lang_input() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn", "Navn er over 200 tegn".padEnd(200, 'a'))))
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt er for langt"))
    }

    @Test
    fun søknadsmottakKontantstøtte_kaster_feil_ved_for_lang_label() {
        val søknad = KontantstøtteSøknadTestData.kontantstøtteSøknad(søker = KontantstøtteSøknadTestData.søker().copy(navn = søknadsfelt("navn".padEnd(200, 'a'), "Navn er over 200 tegn")))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt(label) er for langt"))
    }

    @Test
    fun søknadsmottakBarnetrygd_kaster_feil_ved_for_lang_label() {
        val søknad = BarnetrygdSøknadTestData.barnetrygdSøknad(søker = BarnetrygdSøknadTestData.søker().copy(navn = søknadsfelt("navn".padEnd(200, 'a'), "Navn")))
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                søknad.valider()
            }

        assertTrue(exception.message!!.startsWith("Tekstfelt(label) er for langt"))
    }
}
