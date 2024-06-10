package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PersonopplysningerController(
    private val personopplysningerService: PersonopplysningerService
) {
    @GetMapping("/personopplysning")
    fun personInfo(
        @RequestParam ytelse: Ytelse
    ): ResponseEntity<Ressurs<Person?>> =
        ResponseEntity.ok(
            Ressurs.success(
                personopplysningerService.hentPersoninfo(
                    EksternBrukerUtils.hentFnrFraToken(),
                    ytelse
                )
            )
        )
}
