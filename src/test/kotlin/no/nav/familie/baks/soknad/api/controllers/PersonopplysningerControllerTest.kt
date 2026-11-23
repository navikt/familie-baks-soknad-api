package no.nav.familie.baks.soknad.api.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.domene.Sivilstand
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpStatus

class PersonopplysningerControllerTest {
    private val service = mockk<PersonopplysningerService>()
    private val controller = PersonopplysningerController(service)

    @AfterEach
    fun ryddOpp() {
        unmockkObject(EksternBrukerUtils)
    }

    @ParameterizedTest
    @EnumSource(Ytelse::class)
    fun `personoppslag bruker identitet fra token og valgt ytelse`(ytelse: Ytelse) {
        val person = Person("token-subject", "Testperson", emptySet(), emptyList(), Sivilstand(null), null, false)
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns "token-subject"
        every { service.hentPersoninfo("token-subject", ytelse, false) } returns person

        val response = controller.personInfo(ytelse)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).isEqualTo(person)
        verify(exactly = 1) { service.hentPersoninfo("token-subject", ytelse, false) }
    }
}
