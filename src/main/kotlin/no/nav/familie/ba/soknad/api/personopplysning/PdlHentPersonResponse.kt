package no.nav.familie.ba.soknad.api.personopplysning

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


data class PdlHentPersonResponse(val data: PdlPerson,
                                 val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }
}

data class PdlPerson(val person: PdlPersonData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonData(val navn: List<PdlNavn>,
                         val familierelasjoner: List<PdlFamilierelasjon> = emptyList())

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
