package no.nav.familie.ba.soknad.api.personopplysning

data class Person(
    val navn: String,
    val barn: Set<Barn>,
    val statsborgerskap: List<Statborgerskap>,
    val siviltstatus: Sivilstand
)

data class Barn(
    val ident: String,
    val navn: String,
    val borMedSøker: Boolean,
    val fødselsdato: String
)

data class Statborgerskap(
    val landkode: String
)

data class Sivilstand(
    val type: SIVILSTANDTYPE?
)

enum class SIVILSTANDTYPE {
    GIFT,
    ENKE_ELLER_ENKEMANN,
    SKILT,
    SEPARERT,
    REGISTRERT_PARTNER,
    SEPARERT_PARTNER,
    SKILT_PARTNER,
    GJENLEVENDE_PARTNER
}
