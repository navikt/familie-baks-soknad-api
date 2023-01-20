package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Person
import no.nav.familie.baks.soknad.api.domene.Ytelse
import no.nav.familie.baks.soknad.api.services.pdl.PersonopplysningerService
import no.nav.familie.baks.soknad.api.util.TokenBehandler
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = "tokenx", claimMap = ["acr=Level4"]),
    ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
)
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PersonopplysningerController(private val personopplysningerService: PersonopplysningerService) {

    // TODO: Fjerne denne når ba-soknad og ks-soknad er oppdatert
    @PostMapping("/personopplysning")
    fun personInfo(): ResponseEntity<Ressurs<Person?>> {
        return ResponseEntity.ok(
            Ressurs.success(
                personopplysningerService.hentPersoninfo(
                    TokenBehandler.hentFnr(),
                    Ytelse.BARNETRYGD
                )
            )
        )
    }

    @GetMapping("/personopplysning")
    fun personInfo(@RequestParam ytelse: Ytelse): ResponseEntity<Ressurs<Person?>> {
        return ResponseEntity.ok(
            Ressurs.success(
                personopplysningerService.hentPersoninfo(
                    TokenBehandler.hentFnr(),
                    ytelse
                )
            )
        )
    }
}
