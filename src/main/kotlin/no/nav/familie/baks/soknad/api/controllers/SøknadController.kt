package no.nav.familie.baks.soknad.api.controllers

import no.nav.familie.baks.soknad.api.domene.Kvittering
import no.nav.familie.baks.soknad.api.services.BarnetrygdSøknadService
import no.nav.familie.baks.soknad.api.services.KontantstøtteSøknadService
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.søknad.Søknadsfelt
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ba.søknad.v10.BarnetrygdSøknad as BarnetrygdSøknadV10
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad as BarnetrygdSøknadV9
import no.nav.familie.kontrakter.ks.søknad.v5.KontantstøtteSøknad as KontantstøtteSøknadV5
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad as KontantstøtteSøknadV6

@RestController
@RequestMapping(path = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(
    private val kontantstøtteSøknadService: KontantstøtteSøknadService,
    private val barnetrygdSøknadService: BarnetrygdSøknadService
) {
    private val logger = LoggerFactory.getLogger(SøknadController::class.java)
    protected val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @PostMapping("/soknad/v10")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV10
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            søknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av barnetrygd-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av barnetrygd-søknad feilet", e)
        }
        return ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
    }

    @PostMapping("/soknad/v9")
    fun søknadsmottakBarnetrygd(
        @RequestBody(required = true) søknad: BarnetrygdSøknadV9
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            søknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av barnetrygd-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av barnetrygd-søknad feilet", e)
        }
        return ResponseEntity.ok().body(barnetrygdSøknadService.mottaOgSendBarnetrygdsøknad(søknad))
    }

    @PostMapping("/soknad/kontantstotte/v6")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV6
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            kontantstøtteSøknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av kontantstøtte-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av kontantstøtte-søknad feilet", e)
        }

        return ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
    }

    @PostMapping("/soknad/kontantstotte/v5")
    fun søknadsmottakKontantstøtte(
        @RequestBody(required = true)
        kontantstøtteSøknad: KontantstøtteSøknadV5
    ): ResponseEntity<Ressurs<Kvittering>> {
        try {
            kontantstøtteSøknad.valider()
        } catch (e: Exception) {
            logger.info("Validering av kontantstøtte-søknad feilet. Søknaden sendes videre til journalføring, men man bør se på hvorfor det feiler. Se securelogs for detaljer.")
            secureLogger.info("Validering av kontantstøtte-søknad feilet", e)
        }

        return ResponseEntity.ok().body(kontantstøtteSøknadService.mottaOgSendKontantstøttesøknad(kontantstøtteSøknad))
    }
}

fun BarnetrygdSøknadV10.valider() {
    // valider ident (forhindre SQL/NoSQL injection)
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn
        ).forEach { textField ->
            // valider alle verdier i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
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
        // valider alle labler i tekstfelt
        validerVerdiITextfelt(textField)
        // valider alle verdier i tekstfelt
        validerLabel(textField)
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
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }
}

fun BarnetrygdSøknadV9.valider() {
    // valider ident (forhindre SQL/NoSQL injection)
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn
        ).forEach { textField ->
            // valider alle verdier i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
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
        // valider alle labler i tekstfelt
        validerVerdiITextfelt(textField)
        // valider alle verdier i tekstfelt
        validerLabel(textField)
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
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }
}

fun KontantstøtteSøknadV6.valider() {
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn,
            barn.adresse
        ).forEach { textField ->
            // valider alle verdier i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
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
        validerVerdiITextfelt(textField)
        // valider alle labler i tekstfelt
        validerLabel(textField)
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
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }
}

fun KontantstøtteSøknadV5.valider() {
    søker.ident.verdi.values.forEach { fnr ->
        require(fnr.all { it.isDigit() }) { "Ugyldig format på søker fødselsnummer" }
    }

    barn.forEach { barn ->
        barn.ident.verdi.values.forEach { fnr ->
            require(fnr.all { it.isDigit() }) { "Ugyldig format på barnets fødselsnummer" }
        }
        listOfNotNull(
            barn.navn,
            barn.adresse
        ).forEach { textField ->
            // valider alle verdier i tekstfelt
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
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
        validerVerdiITextfelt(textField)
        // valider alle labler i tekstfelt
        validerLabel(textField)
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
            validerVerdiITextfelt(textField)
            // valider alle labler i tekstfelt
            validerLabel(textField)
        }
    }
}

private fun validerLabel(textField: Søknadsfelt<out Any?>) {
    textField.label.values.forEach { label ->
        require(label.length < 200) { "Tekstfelt(label) er for langt. ${textField.label} " }
        require(!Regex("[<>\"]").containsMatchIn(label)) { "Tekstfelt(label) inneholder ugyldige tegn. ${textField.label} " }
    }
}

private fun validerVerdiITextfelt(textField: Søknadsfelt<out Any?>) {
    textField.verdi.values.forEach { verdi ->
        require(verdi.toString().length < 200) { "Tekstfelt er for langt. ${textField.verdi} " }
        require(!Regex("[<>'\"]").containsMatchIn(verdi.toString())) { "Tekstfelt inneholder ugyldige tegn, ${textField.verdi} " }
    }
}
