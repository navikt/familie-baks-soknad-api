package no.nav.familie.baks.soknad.api.clients.pdl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

data class PdlHentPersonResponse(
    val data: PdlPerson,
    val errors: List<PdlError>?,
    val extensions: PdlExtensions?
) {
    fun harFeil(): Boolean = !errors.isNullOrEmpty()

    fun harAdvarsel(): Boolean = !extensions?.warnings.isNullOrEmpty()

    fun errorMessages(): String = errors?.joinToString { it -> it.message } ?: ""
}

data class PdlPerson(
    val person: PdlPersonData?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonData(
    val navn: List<PdlNavn>,
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val forelderBarnRelasjon: List<PdlFamilierelasjon> = emptyList(),
    val foedsel: List<PdlFødselsDato>,
    val bostedsadresse: List<Bostedsadresse?>,
    val statsborgerskap: List<PdlStatsborgerskap>,
    val sivilstand: List<PdlSivilstand>?,
    val doedsfall: List<PdlDoedsafall>?,
    val folkeregisteridentifikator: List<PdlFolkeregisteridentifikator>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFolkeregisteridentifikator(
    val identifikasjonsnummer: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato(
    val foedselsdato: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError(
    val message: String,
    val extensions: PdlErrorExtension?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlErrorExtension(
    val code: String,
    val details: Details?,
    val classification: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlExtensions(
    val warnings: List<PdlWarning>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlWarning(
    val details: Any?,
    val id: String?,
    val message: String?,
    val query: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlDoedsafall(
    val doedsdato: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Details(
    val type: String,
    val cause: String,
    val policy: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlNavn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
) {
    fun fulltNavn(): String =
        when (mellomnavn) {
            null -> "$fornavn $etternavn"
            else -> "$fornavn $mellomnavn $etternavn"
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlStatsborgerskap(
    val land: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlSivilstand(
    val type: SIVILSTANDSTYPE
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFamilierelasjon(
    val relatertPersonsIdent: String? = null,
    val relatertPersonsRolle: FAMILIERELASJONSROLLE
)

enum class SIVILSTANDSTYPE {
    GIFT,
    ENKE_ELLER_ENKEMANN,
    SKILT,
    SEPARERT,
    REGISTRERT_PARTNER,
    SEPARERT_PARTNER,
    SKILT_PARTNER,
    GJENLEVENDE_PARTNER,
    UGIFT,
    UOPPGITT
}

enum class FAMILIERELASJONSROLLE {
    BARN,
    FAR,
    MEDMOR,
    MOR,
    DOEDFOEDT_BARN
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Adressebeskyttelse(
    val gradering: ADRESSEBESKYTTELSEGRADERING
)

enum class ADRESSEBESKYTTELSEGRADERING {
    STRENGT_FORTROLIG_UTLAND, // Kode 19
    FORTROLIG, // Kode 7
    STRENGT_FORTROLIG, // Kode 6
    UGRADERT
}
