package no.nav.familie.ba.soknad.api.personopplysning

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.familie.kontrakter.felles.personinfo.Ident
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PersonopplysningerController(private val personopplysningerService: PersonopplysningerService) {

    @PostMapping("/personopplysning")
    fun personInfo(@RequestBody(required = true) ident: Ident): ResponseEntity<Ressurs<Person>> {
        return ResponseEntity.ok(Ressurs.success(personopplysningerService.hentPersoninfo(ident.ident)))
    }
}