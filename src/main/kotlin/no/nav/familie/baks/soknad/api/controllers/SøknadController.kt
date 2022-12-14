package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.clients.mottak.MottakClient
import no.nav.familie.baks.soknad.api.domene.KontantstøtteSøknad
import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.util.TokenBehandler
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ba.søknad.v8.Søknad as SøknadV8

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"])
class SøknadController(private val mottakClient: MottakClient) {

    @PostMapping("/soknad/v8")
    fun søknadsmottakV8(@RequestBody(required = true) søknad: SøknadV8): ResponseEntity<Ressurs<Kvittering>> {
        val søknadMedIdentFraToken = søknad.copy(
            søker = søknad.søker.copy(
                ident = søknad.søker.ident.copy(
                    verdi = søknad.søker.ident.verdi.mapValues { TokenBehandler.hentFnr() }
                )
            )
        )

        return ResponseEntity.ok().body(mottakClient.sendBarnetrygdSøknad(søknadMedIdentFraToken))
    }

    @PostMapping("/soknad/kontantstotte")
    fun søknadsmottakKontantStotte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknad
    ): ResponseEntity<Ressurs<Kvittering>> {
        val kontantstøtteSøknadMedIdentFraToken = kontantstøtteSøknad.copy(
            søker = kontantstøtteSøknad.søker.copy(
                ident = kontantstøtteSøknad.søker.ident.copy(
                    verdi = kontantstøtteSøknad.søker.ident.verdi.mapValues { TokenBehandler.hentFnr() }
                )
            )
        )

        return ResponseEntity.ok().body(mottakClient.sendKontantstøtteSøknad(kontantstøtteSøknadMedIdentFraToken))
    }
}
