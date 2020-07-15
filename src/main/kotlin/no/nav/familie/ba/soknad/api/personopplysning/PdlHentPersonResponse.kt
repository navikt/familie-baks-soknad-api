package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse

private fun harFeil(errors: List<PdlError>?) = !errors.isNullOrEmpty()

private fun errorMessages(errors: List<PdlError>?): String {
    return errors?.joinToString { it -> it.message } ?: ""
}

data class PdlHentSøkerResponse(val data: PdlSøker,
                                 val errors: List<PdlError>?) {

    fun harFeil(): Boolean = harFeil(errors)

    fun errorMessages(): String = errorMessages(errors)
}

data class PdlHentBarnResponse(val data: PdlBarn,
                                val errors: List<PdlError>?) {

    fun harFeil(): Boolean = harFeil(errors)

    fun errorMessages(): String = errorMessages(errors)
}

data class PdlSøker(val person: PdlSøkerData?)
data class PdlBarn(val person: PdlBarnData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlSøkerData(val navn: List<PdlNavn>,
                         val familierelasjoner: List<PdlFamilierelasjon> = emptyList(),
                         val bostedsadresse: List<Bostedsadresse?>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlBarnData(val navn: List<PdlNavn>,
                         val foedsel: List<PdlFødselsDato>,
                         val bostedsadresse: List<Bostedsadresse?>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato(val foedselsdato: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError(val message: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlNavn(val fornavn: String,
                   val mellomnavn: String? = null,
                   val etternavn: String) {

    fun fulltNavn(): String {
        return when (mellomnavn) {
            null -> "$fornavn $etternavn"
            else -> "$fornavn $mellomnavn $etternavn"
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFamilierelasjon(val relatertPersonsIdent: String,
                              val relatertPersonsRolle: FAMILIERELASJONSROLLE)

enum class FAMILIERELASJONSROLLE {
    BARN,
    FAR,
    MEDMOR,
    MOR
}