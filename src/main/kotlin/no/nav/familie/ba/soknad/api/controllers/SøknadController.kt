package no.nav.familie.ba.soknad.api.controllers

import no.nav.familie.ba.soknad.api.domene.Kvittering
import no.nav.familie.ba.soknad.api.integrasjoner.MottakClient
import no.nav.familie.ba.soknad.api.util.TokenBehandler
import no.nav.familie.kontrakter.ba.søknad.Søknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class SøknadController(private val mottakClient: MottakClient) {

    @PostMapping("/soknad")
    fun søknadsMottak(@RequestBody(required = true) søknad: Søknad): ResponseEntity<Ressurs<Kvittering>> {

        val søknadMedIdentFraToken = søknad.copy(
            søker = søknad.søker.copy(
                ident = søknad.søker.ident.copy(
                    verdi = TokenBehandler.hentFnr()
                )
            )
        )

        return ResponseEntity.ok().body(mottakClient.sendSøknad(søknadMedIdentFraToken))
    }
}
