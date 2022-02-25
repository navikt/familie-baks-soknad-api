package no.nav.familie.ba.soknad.api.domene

import no.nav.familie.kontrakter.ba.søknad.SIVILSTANDTYPE
import no.nav.familie.kontrakter.ba.søknad.SøknadAdresse

data class Person(
        val ident: String,
        val navn: String,
        val barn: Set<Barn>,
        val statsborgerskap: List<Statborgerskap>,
        val sivilstand: Sivilstand,
        val adresse: SøknadAdresse?,
        val adressebeskyttelse: Boolean
)

data class Barn(
        val ident: String,
        val navn: String?,
        val borMedSøker: Boolean,
        val fødselsdato: String?,
        val adressebeskyttelse: Boolean
)

data class Statborgerskap(
        val landkode: String
)

data class Sivilstand(
        val type: SIVILSTANDTYPE?
)
