package no.nav.familie.ba.soknad.api.personopplysning

data class Person(
        val navn: String,
        val barn: Set<Barn>
)

data class Barn(
        val personIdent: Personident,
        val navn: String
)

data class Personident(
        val id: String
)


