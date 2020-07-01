package no.nav.familie.ba.soknad.api.personopplysning

data class PdlPersonRequest (val variables: PdlPersonRequestVariables, val query: String)

data class PdlPersonRequestVariables (var ident: String)