package no.nav.familie.ba.soknad.api.personopplysning

data class Person(
    val navn: String,
    val barn: Set<Barn>,
    val statsborgerskap: List<Statborgerskap>
)

data class Barn(
    val ident: String,
    val navn: String,
    val borMedSøker: Boolean,
    val fødselsdato: String
)


data class Statborgerskap(
        val landkode: String,
)
