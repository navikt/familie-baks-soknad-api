package no.nav.familie.ba.soknad.api.personopplysning

data class Person(
        val navn: String,
        val familierelasjoner: Set<Familierelasjon>
)

data class Familierelasjon(
        val personIdent: Personident,
        val relasjonsrolle: String
)

data class Personident(
        val id: String
)



