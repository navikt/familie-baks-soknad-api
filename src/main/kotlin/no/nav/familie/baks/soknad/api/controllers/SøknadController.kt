package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.kontrakter.felles.Fødselsnummer
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(
    private val kontantstøtteSøknadService: KontantstøtteSøknadService,
    private val barnetrygdSøknadService: BarnetrygdSøknadService
) {
    @PostMapping("/soknad/v9")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV9
    ): ResponseEntity<Ressurs<Kvittering>> {
        søknad.valider()
        return ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
    }

    @PostMapping("/soknad/kontantstotte/v5")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV5
    ): ResponseEntity<Ressurs<Kvittering>> {
        kontantstøtteSøknad.valider()
        return ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
    }
}

fun BarnetrygdSøknadV9.valider() {
    // valider ident (forhindre SQL/NoSQL injection)
    søker.ident.verdi.values.forEach { fnr ->
        val erGyldig = runCatching { Fødselsnummer(fnr) }.isSuccess
        require(erGyldig) { "Ugyldig format på fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            val erGyldig = runCatching { Fødselsnummer(fnr) }.isSuccess
            require(erGyldig) { "Ugyldig format på barnets fødselsnummer" }
        }
    }

    // XSS prevention - sanitize text fields
    listOfNotNull(
        søker.navn,
        søker.statsborgerskap,
        søker.sivilstand,
        søker.adresse,
        søker.nåværendeSamboer
    ).forEach { textField ->
        // valider alle verdier i tekstfelt
        textField.verdi.values.forEach { verdi ->
            require(verdi.toString().length > 500) { "Tekstfelt er for langt" }
            require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn" }
        }
        // valider alle labler i tekstfelt
        textField.label.values.forEach { label ->
            require(label.toString().length > 500) { "Tekstfelt(label) er for langt" }
            require(!Regex("[<>'\"]").containsMatchIn(label.toString())) { "Tekstfelt(label) inneholder ugyldige tegn" }
        }
    }
    listOfNotNull(
        søker.andreUtbetalingsperioder,
        søker.pensjonsperioderNorge,
        søker.idNummer,
        søker.arbeidsperioderNorge,
        søker.pensjonsperioderUtland,
        søker.tidligereSamboere,
        søker.utenlandsperioder,
        søker.arbeidsperioderUtland
    ).forEach { liste ->
        liste.forEach { textField ->
            // valider alle verider i tekstfelt
            textField.verdi.values.forEach { verdi ->
                require(verdi.toString().length > 500) { "Tekstfelt er for langt" }
                require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn" }
            }
            // valider alle labler i tekstfelt
            textField.label.values.forEach { label ->
                require(label.toString().length > 500) { "Tekstfelt(label) er for langt" }
                require(!Regex("[<>'\"]").containsMatchIn(label.toString())) { "Tekstfelt(label) inneholder ugyldige tegn" }
            }
        }
    }
}

fun KontantstøtteSøknadV5.valider() {
    søker.ident.verdi.values.forEach { fnr ->
        val erGyldig = runCatching { Fødselsnummer(fnr) }.isSuccess
        require(erGyldig) { "Ugyldig format på fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            val erGyldig = runCatching { Fødselsnummer(fnr) }.isSuccess
            require(erGyldig) { "Ugyldig format på barnets fødselsnummer" }
        }
    }

    // XSS prevention - sanitize text fields
    listOfNotNull(
        søker.navn,
        søker.statsborgerskap,
        søker.sivilstand,
        søker.adresse
    ).forEach { textField ->
        // valider alle verdier i tekstfelt
        textField.verdi.values.forEach { verdi ->
            require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn" }
        }
        // valider alle labler i tekstfelt
        textField.label.values.forEach { label ->
            require(!Regex("[<>'\"]").containsMatchIn(label.toString())) { "Tekstfelt(label) inneholder ugyldige tegn" }
        }
    }
    listOfNotNull(
        søker.andreUtbetalingsperioder,
        søker.pensjonsperioderNorge,
        søker.idNummer,
        søker.arbeidsperioderNorge,
        søker.pensjonsperioderUtland,
        søker.utenlandsperioder,
        søker.arbeidsperioderUtland
    ).forEach { liste ->
        liste.forEach { textField ->
            // valider alle verider i tekstfelt
            textField.verdi.values.forEach { verdi ->
                require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn" }
            }
            // valider alle labler i tekstfelt
            textField.label.values.forEach { label ->
                require(!Regex("[<>'\"]").containsMatchIn(label.toString())) { "Tekstfelt(label) inneholder ugyldige tegn" }
            }
        }
    }
}
