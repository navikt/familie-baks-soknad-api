package no.nav.familie.baks.soknad.api.kodeverk

import io.mockk.every
import io.mockk.mockk
import java.lang.reflect.Modifier
import java.time.LocalDate
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.javaMethod
import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import no.nav.familie.baks.soknad.api.services.kodeverk.CachedKodeverkService
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkSpråk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable

class CachedKodeverkServiceTest {

    private val kodeverkClientMock: KodeverkClient = mockk()
    private val kodeverkService = CachedKodeverkService(kodeverkClientMock)

    @Test
    fun `skal returnere poststed`() {
        val beskrivelse = BeskrivelseDto(POSTSTED, "")
        val beytning = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelse))
        val kodeverk = KodeverkDto(mapOf(POSTNUMMER to listOf(beytning)))

        every { kodeverkClientMock.hentPostnummer() } returns kodeverk

        val poststedTest = kodeverkService.hentPostnummer()[POSTNUMMER]
        assertThat(poststedTest).isEqualTo(POSTSTED)
    }

    @Test
    fun `skal returnere tom poststed hvis den ikke finnes`() {
        every { kodeverkClientMock.hentPostnummer() } returns KodeverkDto(emptyMap())

        val poststedTest = kodeverkService.hentPostnummer()[POSTNUMMER]
        assertThat(poststedTest).isNull()
    }

    @Test
    fun `alle public metoder skal være annotert med @Cacheable`() {
        val publikMetoderUtenCacheable = CachedKodeverkService::class.declaredMemberFunctions
            .filter { Modifier.isPublic(it.javaMethod!!.modifiers) }
            .filter { it.annotations.none { it.annotationClass == Cacheable::class } }
        assertThat(publikMetoderUtenCacheable).isEmpty()
    }

    companion object {
        private const val POSTNUMMER = "0557"
        private const val POSTSTED = "Oslo"
    }
}
